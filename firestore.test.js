const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const OWNER_UID = "owner_user_1";
const VET_UID = "vet_user_2";
const WORKER_UID = "worker_user_3";
const STRANGER_UID = "stranger_user_4";
const FARM_ID = "farm_1";
const OTHER_FARM_ID = "farm_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

async function seedSharedFarmWithMembers(farmId, membersMap) {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    const now = new Date();
    await db.collection("farms").doc(farmId).set({
      farmId: farmId,
      name: "عنبر البركة النموذجي",
      location: "المدية - سيدي نعمان",
      capacity: 12000,
      assignedWorkerId: WORKER_UID,
      members: membersMap,
      createdAt: now,
      updatedAt: now,
    });
    for (const [uid, role] of Object.entries(membersMap)) {
      await db.collection("users").doc(uid).set({
        userId: uid,
        role: role,
        farmId: farmId,
        createdAt: now,
        updatedAt: now,
      });
    }
  });
}

test("Unauthenticated user: cannot read or write any shared farm data", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("farms").doc(FARM_ID).get());
  await assertFails(unauthDb.collection("farms").doc(FARM_ID).collection("dailyReports").get());
  await assertFails(unauthDb.collection("farms").doc(FARM_ID).collection("transactions").get());
});

test("Cross-farm isolation: user not in farm members cannot read or write another farm's data", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
    [WORKER_UID]: "WORKER",
  });
  await seedSharedFarmWithMembers(OTHER_FARM_ID, {
    [STRANGER_UID]: "OWNER",
  });

  await testEnv.withSecurityRulesDisabled(async (context) => {
    const now = new Date();
    await context.firestore().collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_1").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      dayNumber: 1,
      feedIntakeKg: 240.0,
      waterIntakeLiters: 500.0,
      mortalityCount: 2,
      temperature: 33.0,
      gasUnits: 4.0,
      ventilationStatus: "جيدة",
      litterCondition: "طبيعية",
      createdAt: now,
      updatedAt: now,
    });
  });

  const strangerDb = testEnv.authenticatedContext(STRANGER_UID).firestore();
  await assertFails(strangerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_1").get());
});

test("Shared Farm Collaboration: Owner, Vet, and Worker collaborate on /farms/{farmId}/...", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();

  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const vetDb = testEnv.authenticatedContext(VET_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();

  // 1. Owner creates a cycle in /farms/farm_1/cycles/cycle_2026_01
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_2026_01").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      totalBirdsPlaced: 12000,
      targetWeightKg: 2.3,
      cumulativeBirdsSold: 0,
      feedInventoryKg: 1000.0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. Worker writes a daily report to /farms/farm_1/dailyReports/rep_w1 AND updates feedInventoryKg on the shared cycle
  await assertSucceeds(
    workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_w1").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      dayNumber: 2,
      feedIntakeKg: 300.0,
      waterIntakeLiters: 600.0,
      mortalityCount: 1,
      temperature: 32.0,
      gasUnits: 3.5,
      ventilationStatus: "جيدة",
      litterCondition: "طبيعية",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_2026_01").update({
      feedInventoryKg: 700.0,
      updatedAt: now,
    })
  );

  // 3. Vet reads Worker's daily report and writes a vetNote on the same shared farm
  await assertSucceeds(
    vetDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_w1").get()
  );

  await assertSucceeds(
    vetDb.collection("farms").doc(FARM_ID).collection("vetNotes").doc("vet_v1").set({
      userId: VET_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      dayNumber: 2,
      medication: "Tylosin",
      dosage: "0.5g/L",
      diagnosis: "Respiratory",
      isUrgent: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 4. Worker can read the Vet's note, but CANNOT create vetNotes or transactions
  await assertSucceeds(
    workerDb.collection("farms").doc(FARM_ID).collection("vetNotes").doc("vet_v1").get()
  );

  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("vetNotes").doc("vet_w_illegal").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      dayNumber: 2,
      medication: "Antibiotic",
      dosage: "1g/L",
      diagnosis: "Cough",
      isUrgent: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_w_illegal").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "EXPENSE",
      category: "MEDICATION",
      amount: 10000,
      currency: "دج",
      date: "اليوم 2",
      idempotencyKey: "tx_w_illegal",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("RBAC Vet Medication Invoicing: Vet CAN create EXPENSE + MEDICATION transaction, but CANNOT create other transactions or list treasury", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
  });
  const now = new Date();
  const vetDb = testEnv.authenticatedContext(VET_UID).firestore();

  // Vet CAN create transaction when type == 'EXPENSE' AND category == 'MEDICATION'
  await assertSucceeds(
    vetDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_vet_med_1").set({
      userId: VET_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "EXPENSE",
      category: "MEDICATION",
      amount: 14500.0,
      currency: "دج",
      date: "اليوم 4",
      idempotencyKey: "tx_vet_med_1",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Vet CANNOT create INCOME transaction
  await assertFails(
    vetDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_vet_inc_illegal").set({
      userId: VET_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "INCOME",
      category: "MEDICATION",
      amount: 14500.0,
      currency: "دج",
      date: "اليوم 4",
      idempotencyKey: "tx_vet_inc_illegal",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Vet CANNOT create non-MEDICATION EXPENSE (e.g. FEED)
  await assertFails(
    vetDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_vet_feed_illegal").set({
      userId: VET_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "EXPENSE",
      category: "FEED",
      amount: 80000.0,
      currency: "دج",
      date: "اليوم 4",
      idempotencyKey: "tx_vet_feed_illegal",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Vet CANNOT list all farm transactions
  await assertFails(
    vetDb.collection("farms").doc(FARM_ID).collection("transactions").get()
  );
});

test("RBAC Owner: has full read/write access to farm, cycles, dailyReports, vetNotes, and transactions", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();

  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_owner_1").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "INCOME",
      category: "tx_chicken_sale",
      amount: 570000.0,
      currency: "دج",
      date: "اليوم 5",
      idempotencyKey: "inv_owner_1",
      invoiceNumber: "INV-1-0001",
      buyerName: "كمال رابحي",
      truckPlate: "04521-122-16",
      birdAgeDays: 5,
      totalCrates: 150,
      birdsPerCrate: 10,
      totalBirdsSold: 1500,
      totalGrossKg: 3650.0,
      totalTareKg: 2150.0,
      totalNetKg: 1500.0,
      avgBirdWeightKg: 1.0,
      pricePerKg: 380.0,
      tareWeightsCount: 5,
      grossWeightsCount: 15,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("transactions").get()
  );
});

test("Security Audit: Stranger cannot escalate to OWNER via /users profile or QR self-join, and Worker can submit PENDING expense request", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const strangerDb = testEnv.authenticatedContext(STRANGER_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();

  // Stranger writes forged OWNER profile for FARM_ID in their own /users doc
  await assertSucceeds(
    strangerDb.collection("users").doc(STRANGER_UID).set({
      userId: STRANGER_UID,
      role: "OWNER",
      farmId: FARM_ID,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Stranger still CANNOT read or write /farms/farm_1/transactions because they are not in /farms/farm_1.members
  await assertFails(
    strangerDb.collection("farms").doc(FARM_ID).collection("transactions").get()
  );

  // Stranger CANNOT add themselves as OWNER to /farms/farm_1.members
  await assertFails(
    strangerDb.collection("farms").doc(FARM_ID).update({
      members: {
        [OWNER_UID]: "OWNER",
        [WORKER_UID]: "WORKER",
        [STRANGER_UID]: "OWNER",
      },
      updatedAt: now,
    })
  );

  // Worker CAN submit a PENDING financial request (e.g. emergency gas/advance)
  await assertSucceeds(
    workerDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_worker_req_1").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "EXPENSE",
      category: "شراء غاز / صيانة طارئة",
      amount: 12000.0,
      currency: "دج",
      date: "اليوم 4",
      idempotencyKey: "tx_worker_req_1",
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Worker CANNOT approve their own PENDING transaction
  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_worker_req_1").update({
      status: "APPROVED",
      updatedAt: now,
    })
  );

  // Owner CAN approve the Worker's PENDING transaction
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_worker_req_1").update({
      status: "APPROVED",
      updatedAt: now,
    })
  );
});

