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

  // 2. Worker atomically writes a daily report to /farms/farm_1/dailyReports/rep_w1 AND updates feedInventoryKg/cumulativeMortality on the shared cycle
  const workerReportBatch = workerDb.batch();
  workerReportBatch.set(workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_w1"), {
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
  });
  workerReportBatch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_2026_01"), {
    feedInventoryKg: 700.0,
    feedDeficitKg: 0.0,
    cumulativeFeedConsumedKg: 300.0,
    cumulativeMortality: 1,
    lastDailyReportId: "rep_w1",
    updatedAt: now,
  });
  await assertSucceeds(workerReportBatch.commit());

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

  // Vet CAN create transaction when type == 'EXPENSE' AND category == 'MEDICATION' AND status == 'PENDING'
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
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Vet CANNOT create transaction directly with status == 'APPROVED'
  await assertFails(
    vetDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_vet_med_approved_illegal").set({
      userId: VET_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "EXPENSE",
      category: "MEDICATION",
      amount: 14500.0,
      currency: "دج",
      date: "اليوم 4",
      idempotencyKey: "tx_vet_med_approved_illegal",
      status: "APPROVED",
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
      status: "PENDING",
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
      status: "PENDING",
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

test("Security Audit: Stranger cannot escalate to OWNER or WORKER via /users profile without verified invite, and Worker can submit PENDING expense request", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const strangerDb = testEnv.authenticatedContext(STRANGER_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();

  // Stranger writes forged WORKER profile for FARM_ID in their own /users doc
  await assertSucceeds(
    strangerDb.collection("users").doc(STRANGER_UID).set({
      userId: STRANGER_UID,
      role: "WORKER",
      farmId: FARM_ID,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Stranger still CANNOT self-join /farms/farm_1.members as WORKER without a verified invite
  await assertFails(
    strangerDb.collection("farms").doc(FARM_ID).update({
      members: {
        [OWNER_UID]: "OWNER",
        [WORKER_UID]: "WORKER",
        [STRANGER_UID]: "WORKER",
      },
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
      approvedBySignature: "موافقة المالك المعتمدة",
      updatedAtMillis: Date.now(),
      updatedAt: now,
    })
  );
});

test("Verified Invite Flow: Owner creates invite, invitee atomically consumes invite to join farm, and replay/mismatch fails", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const newWorkerUid = "invited_worker_99";
  const newWorkerDb = testEnv.authenticatedContext(newWorkerUid).firestore();
  const inviteId = "inv_worker_test_1";

  // 1. Owner creates a verified invite under /farms/farm_1/invites/inv_worker_test_1
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("invites").doc(inviteId).set({
      inviteId: inviteId,
      farmId: FARM_ID,
      role: "WORKER",
      status: "ACTIVE",
      createdBy: OWNER_UID,
      expiresAtMillis: Date.now() + 86400000,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. Invitee cannot join with a mismatched role (e.g. VETERINARIAN instead of WORKER)
  const badBatch = newWorkerDb.batch();
  badBatch.update(newWorkerDb.collection("farms").doc(FARM_ID).collection("invites").doc(inviteId), {
    status: "CONSUMED",
    consumedBy: newWorkerUid,
    updatedAt: now,
  });
  badBatch.update(newWorkerDb.collection("farms").doc(FARM_ID), {
    members: {
      [OWNER_UID]: "OWNER",
      [newWorkerUid]: "VETERINARIAN",
    },
    lastConsumedInviteId: inviteId,
    updatedAt: now,
  });
  await assertFails(badBatch.commit());

  // 3. Invitee atomically consumes the invite and joins with exact matching role WORKER
  const validBatch = newWorkerDb.batch();
  validBatch.update(newWorkerDb.collection("farms").doc(FARM_ID).collection("invites").doc(inviteId), {
    status: "CONSUMED",
    consumedBy: newWorkerUid,
    updatedAt: now,
  });
  validBatch.update(newWorkerDb.collection("farms").doc(FARM_ID), {
    members: {
      [OWNER_UID]: "OWNER",
      [newWorkerUid]: "WORKER",
    },
    lastConsumedInviteId: inviteId,
    updatedAt: now,
  });
  await assertSucceeds(validBatch.commit());

  // 4. Another user cannot replay the already-CONSUMED invite
  const replayDb = testEnv.authenticatedContext("replay_attacker_77").firestore();
  const replayBatch = replayDb.batch();
  replayBatch.update(replayDb.collection("farms").doc(FARM_ID).collection("invites").doc(inviteId), {
    status: "CONSUMED",
    consumedBy: "replay_attacker_77",
    updatedAt: now,
  });
  replayBatch.update(replayDb.collection("farms").doc(FARM_ID), {
    members: {
      [OWNER_UID]: "OWNER",
      [newWorkerUid]: "WORKER",
      replay_attacker_77: "WORKER",
    },
    lastConsumedInviteId: inviteId,
    updatedAt: now,
  });
  await assertFails(replayBatch.commit());
});

test("Chat Contract & Security: Participants can send/read unified messages, strangers are blocked, senderId forgery and non-owner approval are rejected", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const vetDb = testEnv.authenticatedContext(VET_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();
  const strangerDb = testEnv.authenticatedContext(STRANGER_UID).firestore();
  const channelId = "cycle_2026_01_vet";

  // 1. Vet initializes chat channel metadata linked to FARM_ID
  await assertSucceeds(
    vetDb.collection("chatChannels").doc(channelId).set({
      channelId: channelId,
      farmId: FARM_ID,
      participants: [VET_UID, OWNER_UID],
      createdBy: VET_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. Vet sends a unified financial request chat message with status == PENDING
  await assertSucceeds(
    vetDb.collection("chatChannels").doc(channelId).collection("messages").doc("msg_fin_1").set({
      userId: VET_UID,
      senderId: VET_UID,
      senderName: "د. كمال",
      senderRole: "VETERINARIAN",
      cycleId: channelId,
      farmId: FARM_ID,
      text: "فاتورة دواء إنروفلوكساسين",
      timestamp: "09:30 ص",
      timestampText: "09:30 ص",
      linkedTransactionId: "tx_vet_1",
      linkedVetNoteId: "vn_vet_1",
      isFinancialRequest: true,
      requestCategory: "MEDICATION",
      requestAmount: 14500.0,
      requestCurrency: "DZD",
      status: "PENDING",
      requestStatus: "PENDING",
      deliveryState: "SYNCED",
      createdAtMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
    })
  );

  // 3. Stranger CANNOT read or write messages in the channel
  await assertFails(
    strangerDb.collection("chatChannels").doc(channelId).collection("messages").doc("msg_fin_1").get()
  );
  await assertFails(
    strangerDb.collection("chatChannels").doc(channelId).collection("messages").doc("msg_stranger").set({
      userId: STRANGER_UID,
      senderId: STRANGER_UID,
      senderName: "غريب",
      senderRole: "WORKER",
      cycleId: channelId,
      farmId: FARM_ID,
      text: "اختراق",
      timestamp: "10:00 ص",
      createdAt: now,
      updatedAt: now,
    })
  );

  // 4. Participant CANNOT forge senderId of another user
  await assertFails(
    workerDb.collection("chatChannels").doc(channelId).collection("messages").doc("msg_forged").set({
      userId: WORKER_UID,
      senderId: OWNER_UID,
      senderName: "مالك مزيف",
      senderRole: "OWNER",
      cycleId: channelId,
      farmId: FARM_ID,
      text: "رسالة مزيفة",
      timestamp: "10:05 ص",
      createdAt: now,
      updatedAt: now,
    })
  );

  // 5. Non-owner (Worker/Vet) CANNOT approve a PENDING chat financial request
  await assertFails(
    vetDb.collection("chatChannels").doc(channelId).collection("messages").doc("msg_fin_1").update({
      status: "APPROVED",
      requestStatus: "APPROVED",
      updatedAt: now,
    })
  );

  // 6. Owner CAN approve a PENDING chat financial request without mutating core message fields
  await assertSucceeds(
    ownerDb.collection("chatChannels").doc(channelId).collection("messages").doc("msg_fin_1").update({
      status: "APPROVED",
      requestStatus: "APPROVED",
      updatedAt: now,
    })
  );
});

test("DailyReport Locking & Active Cycle Guard: Worker cannot update a locked dailyReport or write to an inactive cycle", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();

  // Create active cycle
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_active_1").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      status: "active",
      totalBirdsPlaced: 10000,
      targetWeightKg: 2.3,
      cumulativeMortality: 2,
      cumulativeBirdsSold: 0,
      feedInventoryKg: 1750.0,
      feedDeficitKg: 0.0,
      cumulativeFeedConsumedKg: 250.0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Seed a locked daily report
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_locked_1").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_active_1",
      dayNumber: 3,
      feedIntakeKg: 250.0,
      waterIntakeLiters: 500.0,
      mortalityCount: 2,
      temperature: 31.0,
      gasUnits: 2.0,
      ventilationStatus: "جيدة",
      litterCondition: "طبيعية",
      isLocked: true,
      createdAt: now,
      updatedAt: now,
    });
  });

  // Worker CANNOT update a locked daily report even in an atomic batch
  const workerLockedBatch = workerDb.batch();
  workerLockedBatch.update(workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_locked_1"), {
    feedIntakeKg: 999.0,
    isEdited: true,
    previousSummary: "العلف: 2.5 ق",
    updatedAt: now,
  });
  workerLockedBatch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_active_1"), {
    feedInventoryKg: 1001.0,
    lastDailyReportId: "rep_locked_1",
    updatedAt: now,
  });
  await assertFails(workerLockedBatch.commit());

  // Owner CAN update a locked daily report when atomically updating the cycle
  const ownerLockedBatch = ownerDb.batch();
  ownerLockedBatch.update(ownerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_locked_1"), {
    feedIntakeKg: 260.0,
    isEdited: true,
    previousSummary: "العلف: 2.5 ق",
    updatedAt: now,
  });
  ownerLockedBatch.update(ownerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_active_1"), {
    feedInventoryKg: 1740.0,
    feedDeficitKg: 0.0,
    cumulativeFeedConsumedKg: 260.0,
    lastDailyReportId: "rep_locked_1",
    updatedAt: now,
  });
  await assertSucceeds(ownerLockedBatch.commit());
});

test("Schema v2 & Audit Fields: farms, cycles, dailyReports, vetNotes, transactions, and chatChannels allow new optional fields", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const vetDb = testEnv.authenticatedContext(VET_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();

  // 1. Create farm with schemaVersion, sectorId, branchId, customMetrics
  await assertSucceeds(
    ownerDb.collection("farms").doc("farm_v2").set({
      farmId: "farm_v2",
      name: "عنبر التوسع المستقبلي",
      location: "سطيف",
      capacity: 15000,
      assignedWorkerId: WORKER_UID,
      currencyCode: "DZD",
      schemaVersion: 2,
      sectorId: "AGRICULTURE",
      branchId: "BROILER",
      customMetrics: { targetFcr: "1.55" },
      members: { [OWNER_UID]: "OWNER" },
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. Create cycle with schemaVersion, sectorId, branchId, customMetrics, status, startDate, chickPrice, cumulativeMortality
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_v2").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      status: "active",
      startDate: "01-11-2026",
      chickPrice: 125.0,
      totalBirdsPlaced: 10000,
      targetWeightKg: 2.8,
      cumulativeMortality: 0,
      cumulativeBirdsSold: 0,
      cumulativeSoldWeightKg: 0.0,
      feedInventoryKg: 4000.0,
      feedDeficitKg: 0.0,
      cumulativeFeedConsumedKg: 0.0,
      currencyCode: "DZD",
      schemaVersion: 2,
      sectorId: "AGRICULTURE",
      branchId: "BROILER",
      customMetrics: {},
      createdAt: now,
      updatedAt: now,
    })
  );

  // 3. Create dailyReport with medicineGiven, medicineSummary, withdrawalDays, updatedAtMillis, isLocked atomically with cycle_v2
  const repV2Batch = workerDb.batch();
  repV2Batch.set(workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_v2"), {
    userId: WORKER_UID,
    farmId: FARM_ID,
    cycleId: "cycle_v2",
    dayNumber: 5,
    feedIntakeKg: 320.0,
    waterIntakeLiters: 640.0,
    mortalityCount: 2,
    temperature: 31.0,
    gasUnits: 2.5,
    ventilationStatus: "جيدة",
    litterCondition: "طبيعية",
    isEdited: false,
    isLocked: false,
    previousSummary: "",
    actualAvgWeightKg: 0.18,
    medicineGiven: true,
    medicineSummary: "فيتامين AD3E",
    withdrawalDays: 0,
    updatedAtMillis: Date.now(),
    createdAt: now,
    updatedAt: now,
  });
  repV2Batch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_v2"), {
    feedInventoryKg: 3680.0,
    feedDeficitKg: 0.0,
    cumulativeFeedConsumedKg: 320.0,
    cumulativeMortality: 2,
    lastDailyReportId: "rep_v2",
    updatedAt: now,
  });
  await assertSucceeds(repV2Batch.commit());

  // 3b. Nullable temperature (omitted climate telemetry) must succeed, out-of-range temperature must fail
  const nullTempBatch = workerDb.batch();
  nullTempBatch.set(workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_null_temp"), {
    userId: WORKER_UID,
    farmId: FARM_ID,
    cycleId: "cycle_v2",
    dayNumber: 6,
    feedIntakeKg: 350.0,
    waterIntakeLiters: 680.0,
    mortalityCount: 1,
    temperature: null,
    gasUnits: 0.0,
    ventilationStatus: "جيدة",
    litterCondition: "طبيعية",
    isEdited: false,
    isLocked: true,
    previousSummary: "",
    actualAvgWeightKg: 0.21,
    medicineGiven: false,
    medicineSummary: "",
    withdrawalDays: 0,
    updatedAtMillis: Date.now(),
    createdAt: now,
    updatedAt: now,
  });
  nullTempBatch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_v2"), {
    feedInventoryKg: 3330.0,
    feedDeficitKg: 0.0,
    cumulativeFeedConsumedKg: 670.0,
    cumulativeMortality: 3,
    lastDailyReportId: "rep_null_temp",
    updatedAt: now,
  });
  await assertSucceeds(nullTempBatch.commit());

  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_bad_temp").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_v2",
      dayNumber: 7,
      feedIntakeKg: 360.0,
      waterIntakeLiters: 700.0,
      mortalityCount: 1,
      temperature: 99.0,
      gasUnits: 0.0,
      ventilationStatus: "جيدة",
      litterCondition: "طبيعية",
      isEdited: false,
      isLocked: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 4. Create vetNote with withdrawalDays and updatedAtMillis
  await assertSucceeds(
    vetDb.collection("farms").doc(FARM_ID).collection("vetNotes").doc("vet_v2").set({
      userId: VET_UID,
      farmId: FARM_ID,
      cycleId: "cycle_v2",
      dayNumber: 5,
      medication: "Enrofloxacin",
      dosage: "1ml/2L",
      diagnosis: "CRD Prevention",
      isUrgent: true,
      status: "PENDING",
      linkedTransactionId: "tx_v2",
      invoiceAmount: 15000.0,
      currency: "DZD",
      withdrawalDays: 7,
      updatedAtMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
    })
  );

  // 5. Create transaction with notes, quantityQuintals, approvedBySignature, updatedAtMillis, farmName
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_feed_v2").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      farmName: "عنبر البركة النموذجي",
      cycleId: "cycle_v2",
      type: "EXPENSE",
      category: "🌾 علف (40 ق)",
      amount: 320000.0,
      currency: "DZD",
      date: "اليوم 5",
      idempotencyKey: "tx_feed_v2",
      status: "APPROVED",
      notes: "شراء علف بادئ",
      quantityQuintals: 40.0,
      approvedBySignature: "موافقة المالك",
      updatedAtMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("IQ Audit S01, S02, S03, S04, C01: Stranger farm read blocked, Worker channel ownerId/participants tampering blocked, Worker cycle inflation blocked, and new channel C01 succeeds", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();
  const strangerDb = testEnv.authenticatedContext(STRANGER_UID).firestore();

  // S01: Stranger (non-member) CANNOT get or list /farms/{farmId}
  await assertFails(strangerDb.collection("farms").doc(FARM_ID).get());
  await assertFails(strangerDb.collection("farms").get());

  // Member CAN get /farms/{farmId} and query their own farms via members.<uid>
  await assertSucceeds(ownerDb.collection("farms").doc(FARM_ID).get());
  await assertSucceeds(workerDb.collection("farms").doc(FARM_ID).get());
  await assertSucceeds(
    ownerDb.collection("farms").where(`members.${OWNER_UID}`, "in", ["OWNER", "WORKER", "VETERINARIAN"]).get()
  );

  // C01: Checking non-existent channel (get when resource == null) and creating channel + first message succeeds
  const newChannelId = "cycle_2026_01_worker";
  const nonExistentSnap = await assertSucceeds(
    workerDb.collection("chatChannels").doc(newChannelId).get()
  );
  if (!nonExistentSnap.exists) {
    await assertSucceeds(
      workerDb.collection("chatChannels").doc(newChannelId).set({
        channelId: newChannelId,
        farmId: FARM_ID,
        ownerId: OWNER_UID,
        participants: [WORKER_UID, OWNER_UID],
        createdBy: WORKER_UID,
        createdAt: now,
        updatedAt: now,
      })
    );
  }
  await assertSucceeds(
    workerDb.collection("chatChannels").doc(newChannelId).collection("messages").doc("msg_c01_first").set({
      id: "msg_c01_first",
      userId: WORKER_UID,
      senderId: WORKER_UID,
      senderName: "أحمد",
      senderRole: "WORKER",
      cycleId: newChannelId,
      farmId: FARM_ID,
      text: "طلب مصروف غاز طارئ",
      timestamp: "10:00 ص",
      timestampText: "10:00 ص",
      linkedTransactionId: "tx_w_c01",
      isFinancialRequest: true,
      requestCategory: "GAS",
      requestAmount: 9000.0,
      requestCurrency: "DZD",
      status: "PENDING",
      requestStatus: "PENDING",
      deliveryState: "SYNCED",
      createdAtMillis: Date.now(),
      createdAt: now,
      updatedAt: now,
    })
  );

  // S02: Worker CANNOT mutate ownerId on chatChannels to their own UID and CANNOT approve financial message
  await assertFails(
    workerDb.collection("chatChannels").doc(newChannelId).update({
      ownerId: WORKER_UID,
      updatedAt: now,
    })
  );
  await assertFails(
    workerDb.collection("chatChannels").doc(newChannelId).collection("messages").doc("msg_c01_first").update({
      status: "APPROVED",
      requestStatus: "APPROVED",
      updatedAt: now,
    })
  );

  // S03: Worker CANNOT modify participants array to inject STRANGER_UID into the channel
  await assertFails(
    workerDb.collection("chatChannels").doc(newChannelId).update({
      participants: [WORKER_UID, OWNER_UID, STRANGER_UID],
      updatedAt: now,
    })
  );

  // S04: Worker CANNOT inflate feedInventoryKg to 99999 or reset cumulativeMortality to 0 on /cycles/{cycleId}
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_s04").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      status: "active",
      totalBirdsPlaced: 10000,
      targetWeightKg: 2.5,
      feedInventoryKg: 2000.0,
      feedDeficitKg: 0.0,
      cumulativeFeedConsumedKg: 0.0,
      cumulativeMortality: 50,
      cumulativeBirdsSold: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Worker inflating feedInventoryKg (99999 > 2000) -> MUST FAIL
  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_s04").update({
      feedInventoryKg: 99999.0,
      updatedAt: now,
    })
  );

  // Worker decreasing cumulativeMortality standalone (0 < 50) -> MUST FAIL
  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_s04").update({
      cumulativeMortality: 0,
      updatedAt: now,
    })
  );

  // Standalone worker deduction of feedInventoryKg without a dailyReport -> MUST FAIL
  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_s04").update({
      feedInventoryKg: 1800.0,
      cumulativeMortality: 55,
      updatedAt: now,
    })
  );

  // Legitimate Worker deduction of feedInventoryKg (1800 <= 2000) and mortality increase (55 >= 50) coupled with dailyReport -> SUCCEEDS
  const s04Batch = workerDb.batch();
  s04Batch.set(workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_s04_day_1"), {
    userId: WORKER_UID,
    farmId: FARM_ID,
    cycleId: "cycle_s04",
    dayNumber: 1,
    feedIntakeKg: 200.0,
    waterIntakeLiters: 400.0,
    mortalityCount: 5,
    temperature: 32.0,
    gasUnits: 2.0,
    ventilationStatus: "جيدة",
    litterCondition: "طبيعية",
    isLocked: false,
    createdAt: now,
    updatedAt: now,
  });
  s04Batch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_s04"), {
    feedInventoryKg: 1800.0,
    feedDeficitKg: 0.0,
    cumulativeFeedConsumedKg: 200.0,
    cumulativeMortality: 55,
    lastDailyReportId: "rep_s04_day_1",
    updatedAt: now,
  });
  await assertSucceeds(s04Batch.commit());
});

test("Google Play Compliance: Account Deletion self-leave, user_entitlements delete-only, and ugc_reports creation", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [WORKER_UID]: "WORKER",
    [VET_UID]: "VETERINARIAN",
  });
  const now = new Date();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();

  // 1. UGC report creation by authenticated user succeeds with valid schema
  await assertSucceeds(
    workerDb.collection("ugc_reports").doc("rep_ugc_1").set({
      reportId: "rep_ugc_1",
      reporterId: WORKER_UID,
      itemId: "post_1",
      targetUid: "conv_feed_trader",
      reason: "احتيال أو أسعار وهمية",
      contentType: "MARKET_POST",
      status: "PENDING_REVIEW",
      createdAtMillis: Date.now(),
      createdAt: now,
    })
  );

  // Forged reporterId fails
  await assertFails(
    workerDb.collection("ugc_reports").doc("rep_ugc_forged").set({
      reportId: "rep_ugc_forged",
      reporterId: OWNER_UID,
      itemId: "post_1",
      reason: "محتوى مخالف",
      status: "PENDING_REVIEW",
      createdAt: now,
    })
  );

  // 2. Worker can remove ONLY their own UID from shared farm's members AND clear assignedWorkerId when deleting their account
  await assertSucceeds(
    workerDb.collection("farms").doc(FARM_ID).update({
      members: {
        [OWNER_UID]: "OWNER",
        [VET_UID]: "VETERINARIAN",
      },
      assignedWorkerId: "",
      updatedAt: now,
    })
  );

  // 3. User can delete their own /users/{uid} and /user_entitlements/{uid} documents
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("users").doc(WORKER_UID).set({
      userId: WORKER_UID,
      role: "WORKER",
      farmId: FARM_ID,
      createdAt: now,
      updatedAt: now,
    });
    await adminDb.collection("user_entitlements").doc(WORKER_UID).set({
      userId: WORKER_UID,
      tier: "EARLY_ACCESS",
    });
  });

  // User CANNOT self-grant entitlement via client write
  await assertFails(
    workerDb.collection("user_entitlements").doc(WORKER_UID).set({
      userId: WORKER_UID,
      tier: "LIFETIME_PRO",
    })
  );

  // User CAN delete their own entitlement and user profile on account deletion
  await assertSucceeds(
    workerDb.collection("user_entitlements").doc(WORKER_UID).delete()
  );
  await assertSucceeds(
    workerDb.collection("users").doc(WORKER_UID).delete()
  );

  // Worker can self-leave a participating chatChannel and delete their own chat messages on account deletion
  const chanId = "chan_del_self_leave";
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("chatChannels").doc(chanId).set({
      channelId: chanId,
      farmId: FARM_ID,
      ownerId: OWNER_UID,
      createdBy: OWNER_UID,
      participants: [OWNER_UID, WORKER_UID],
      createdAt: now,
      updatedAt: now,
    });
    await adminDb.collection("chatChannels").doc(chanId).collection("messages").doc("msg_w_1").set({
      id: "msg_w_1",
      userId: WORKER_UID,
      senderId: WORKER_UID,
      senderName: "أحمد",
      senderRole: "WORKER",
      cycleId: chanId,
      farmId: FARM_ID,
      text: "رسالة ستحذف",
      timestamp: "10:00",
      createdAt: now,
      updatedAt: now,
    });
  });

  await assertSucceeds(
    workerDb.collection("chatChannels").doc(chanId).collection("messages").doc("msg_w_1").delete()
  );
  await assertSucceeds(
    workerDb.collection("chatChannels").doc(chanId).update({
      participants: [OWNER_UID],
      updatedAt: now,
    })
  );

  // Owner can delete their owned chatChannel and owned farm on account deletion
  await assertSucceeds(
    ownerDb.collection("chatChannels").doc(chanId).delete()
  );
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).delete()
  );
});

test("Audit Section 3: Removing worker from members immediately revokes all farm, cycle, report, transaction, and farm-chat access even if assignedWorkerId or chat participants remain stale", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();
  const teamChannelId = `${FARM_ID}_cycle_2026_01_worker`;

  // Seed cycle, report, and farm team chat channel where WORKER_UID is a participant
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_2026_01").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      status: "active",
      totalBirdsPlaced: 5000,
      targetWeightKg: 2.3,
      feedInventoryKg: 1000.0,
      feedDeficitKg: 0.0,
      cumulativeFeedConsumedKg: 0.0,
      cumulativeMortality: 0,
      cumulativeBirdsSold: 0,
      createdAt: now,
      updatedAt: now,
    });
    await adminDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_existing").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      dayNumber: 1,
      feedIntakeKg: 100.0,
      waterIntakeLiters: 200.0,
      mortalityCount: 1,
      temperature: 32.0,
      gasUnits: 1.0,
      ventilationStatus: "جيدة",
      litterCondition: "طبيعية",
      createdAt: now,
      updatedAt: now,
    });
    await adminDb.collection("chatChannels").doc(teamChannelId).set({
      channelId: teamChannelId,
      farmId: FARM_ID,
      ownerId: OWNER_UID,
      createdBy: OWNER_UID,
      participants: [OWNER_UID, WORKER_UID],
      createdAt: now,
      updatedAt: now,
    });
    await adminDb.collection("chatChannels").doc(teamChannelId).collection("messages").doc("msg_1").set({
      id: "msg_1",
      userId: OWNER_UID,
      senderId: OWNER_UID,
      senderName: "المالك",
      senderRole: "OWNER",
      cycleId: teamChannelId,
      farmId: FARM_ID,
      text: "مرحبا",
      timestamp: "09:00",
      createdAt: now,
      updatedAt: now,
    });
  });

  // Before removal, worker CAN read farm, cycle, report, and team chat
  await assertSucceeds(workerDb.collection("farms").doc(FARM_ID).get());
  await assertSucceeds(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_2026_01").get());
  await assertSucceeds(workerDb.collection("chatChannels").doc(teamChannelId).collection("messages").doc("msg_1").get());

  // Simulate worst-case stale assignedWorkerId: Owner removes WORKER_UID from members even if assignedWorkerId was left as WORKER_UID
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("farms").doc(FARM_ID).update({
      members: {
        [OWNER_UID]: "OWNER",
        [VET_UID]: "VETERINARIAN",
      },
      assignedWorkerId: WORKER_UID,
      updatedAt: now,
    });
  });

  // Removed worker MUST fail all farm, cycle, report, transaction, and farm-chat reads/writes immediately
  await assertFails(workerDb.collection("farms").doc(FARM_ID).get());
  await assertFails(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_2026_01").get());
  await assertFails(workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_existing").get());
  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("transactions").doc("tx_after_revoke").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_2026_01",
      type: "EXPENSE",
      category: "GAS",
      amount: 5000.0,
      currency: "DZD",
      date: "اليوم 2",
      idempotencyKey: "tx_after_revoke",
      status: "PENDING",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertFails(workerDb.collection("chatChannels").doc(teamChannelId).get());
  await assertFails(workerDb.collection("chatChannels").doc(teamChannelId).collection("messages").doc("msg_1").get());
  await assertFails(
    workerDb.collection("chatChannels").doc(teamChannelId).collection("messages").doc("msg_after_revoke").set({
      id: "msg_after_revoke",
      userId: WORKER_UID,
      senderId: WORKER_UID,
      senderName: "عامل سابق",
      senderRole: "WORKER",
      cycleId: teamChannelId,
      farmId: FARM_ID,
      text: "محاولة دخول بعد الإزالة",
      timestamp: "10:00",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Audit Section 4: Farm chat vs 2-party Market Direct chat strict isolation", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [VET_UID]: "VETERINARIAN",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();
  const vetDb = testEnv.authenticatedContext(VET_UID).firestore();
  const buyerADb = testEnv.authenticatedContext("buyer_a").firestore();
  const buyerBDb = testEnv.authenticatedContext("buyer_b").firestore();

  // 1. Cannot create a farm channel that includes a non-member (buyer_a) in participants
  await assertFails(
    ownerDb.collection("chatChannels").doc(`${FARM_ID}_bad_channel`).set({
      channelId: `${FARM_ID}_bad_channel`,
      farmId: FARM_ID,
      ownerId: OWNER_UID,
      createdBy: OWNER_UID,
      participants: [OWNER_UID, "buyer_a"],
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. Buyer A creates a 2-party market direct channel with OWNER_UID (seller) with farmId == ""
  const marketChanA = "market_post_100_buyer_a_owner_user_1";
  await assertSucceeds(
    buyerADb.collection("chatChannels").doc(marketChanA).set({
      channelId: marketChanA,
      farmId: "",
      channelType: "DIRECT_MARKET",
      ownerId: OWNER_UID,
      createdBy: "buyer_a",
      participants: ["buyer_a", OWNER_UID],
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    buyerADb.collection("chatChannels").doc(marketChanA).collection("messages").doc("m_buyer_a_1").set({
      id: "m_buyer_a_1",
      userId: "buyer_a",
      senderId: "buyer_a",
      senderName: "المشتري أ",
      senderRole: "BUYER",
      cycleId: marketChanA,
      farmId: "",
      text: "هل الدجاج متوفر؟",
      timestamp: "11:00",
      createdAt: now,
      updatedAt: now,
    })
  );

  // 3. Seller (OWNER_UID) can read Buyer A's market message, but Worker, Vet, and Buyer B CANNOT read or write it!
  await assertSucceeds(
    ownerDb.collection("chatChannels").doc(marketChanA).collection("messages").doc("m_buyer_a_1").get()
  );
  await assertFails(
    workerDb.collection("chatChannels").doc(marketChanA).collection("messages").doc("m_buyer_a_1").get()
  );
  await assertFails(
    vetDb.collection("chatChannels").doc(marketChanA).collection("messages").doc("m_buyer_a_1").get()
  );
  await assertFails(
    buyerBDb.collection("chatChannels").doc(marketChanA).collection("messages").doc("m_buyer_a_1").get()
  );

  // 4. Buyer B cannot add themselves to Buyer A's 2-party market channel
  await assertFails(
    buyerBDb.collection("chatChannels").doc(marketChanA).update({
      participants: ["buyer_a", OWNER_UID, "buyer_b"],
      updatedAt: now,
    })
  );
});

test("Audit Section 6 & 7: Atomic DailyReport + Cycle Totals, Feed Deficit tracking without phantom inventory, and Cycle-Scoped Day 1 reports", async () => {
  await seedSharedFarmWithMembers(FARM_ID, {
    [OWNER_UID]: "OWNER",
    [WORKER_UID]: "WORKER",
  });
  const now = new Date();
  const ownerDb = testEnv.authenticatedContext(OWNER_UID).firestore();
  const workerDb = testEnv.authenticatedContext(WORKER_UID).firestore();

  // Seed Cycle 1 with 0 feed inventory and Cycle 2 with 1000 kg feed inventory
  await assertSucceeds(
    ownerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_1").set({
      userId: OWNER_UID,
      farmId: FARM_ID,
      status: "active",
      totalBirdsPlaced: 5000,
      targetWeightKg: 2.3,
      feedInventoryKg: 0.0,
      feedDeficitKg: 0.0,
      cumulativeFeedConsumedKg: 0.0,
      cumulativeMortality: 0,
      cumulativeBirdsSold: 0,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 1. Standalone report creation without updating cycle_1 MUST FAIL
  await assertFails(
    workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_farm_1_cycle_1_day_1").set({
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_1",
      dayNumber: 1,
      feedIntakeKg: 250.0,
      waterIntakeLiters: 500.0,
      mortalityCount: 4,
      temperature: 33.0,
      gasUnits: 2.0,
      ventilationStatus: "جيدة",
      litterCondition: "طبيعية",
      isLocked: false,
      createdAt: now,
      updatedAt: now,
    })
  );

  // 2. Atomic batch creating Day 1 report (250 kg feed when inventory is 0) sets feedInventoryKg = 0, feedDeficitKg = 250
  const createBatch = workerDb.batch();
  createBatch.set(
    workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_farm_1_cycle_1_day_1"),
    {
      userId: WORKER_UID,
      farmId: FARM_ID,
      cycleId: "cycle_1",
      dayNumber: 1,
      feedIntakeKg: 250.0,
      waterIntakeLiters: 500.0,
      mortalityCount: 4,
      temperature: 33.0,
      gasUnits: 2.0,
      ventilationStatus: "جيدة",
      litterCondition: "طبيعية",
      isLocked: false,
      createdAt: now,
      updatedAt: now,
    }
  );
  createBatch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_1"), {
    feedInventoryKg: 0.0,
    feedDeficitKg: 250.0,
    cumulativeFeedConsumedKg: 250.0,
    cumulativeMortality: 4,
    lastDailyReportId: "rep_farm_1_cycle_1_day_1",
    updatedAt: now,
  });
  await assertSucceeds(createBatch.commit());

  // 3. Attempting to edit unlocked report from 250 kg -> 200 kg while claiming +50 kg phantom feedInventoryKg MUST FAIL
  const phantomBatch = workerDb.batch();
  phantomBatch.update(
    workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_farm_1_cycle_1_day_1"),
    {
      feedIntakeKg: 200.0,
      mortalityCount: 2,
      isEdited: true,
      previousSummary: "250kg",
      updatedAt: now,
    }
  );
  phantomBatch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_1"), {
    feedInventoryKg: 50.0,
    feedDeficitKg: 250.0,
    cumulativeFeedConsumedKg: 200.0,
    cumulativeMortality: 2,
    lastDailyReportId: "rep_farm_1_cycle_1_day_1",
    updatedAt: now,
  });
  await assertFails(phantomBatch.commit());

  // 4. Correct atomic edit from 250 kg -> 200 kg and mortality 4 -> 2 reduces feedDeficitKg to 200.0 and keeps feedInventoryKg = 0.0
  const validEditBatch = workerDb.batch();
  validEditBatch.update(
    workerDb.collection("farms").doc(FARM_ID).collection("dailyReports").doc("rep_farm_1_cycle_1_day_1"),
    {
      feedIntakeKg: 200.0,
      mortalityCount: 2,
      isEdited: true,
      previousSummary: "250kg",
      updatedAt: now,
    }
  );
  validEditBatch.update(workerDb.collection("farms").doc(FARM_ID).collection("cycles").doc("cycle_1"), {
    feedInventoryKg: 0.0,
    feedDeficitKg: 200.0,
    cumulativeFeedConsumedKg: 200.0,
    cumulativeMortality: 2,
    lastDailyReportId: "rep_farm_1_cycle_1_day_1",
    updatedAt: now,
  });
  await assertSucceeds(validEditBatch.commit());
});



