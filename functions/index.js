

const {onDocumentCreated} = require ("firebase-functions/v2/firestore");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");
const functions = require("firebase-functions/v2");
functions.setGlobalOptions({region: "europe-west1"});

const admin = require("firebase-admin");
admin.initializeApp();

exports.sendNotificationV2 = functions.https.onCall(async (data, context) => {
  const { token, title, body } = data.data;

const message = {
  token: token,
  data: {
    title: title,
    body: body,
    type: data.data?.type || "",
    target_user_id: data.data?.target_user_id || "",
    travel_id: data.data?.travel_id?.toString() || ""
  },
  android: { priority: "high" }
};

  // 3. Invio
  try {
    const response = await admin.messaging().send(message);
    console.log("✅ Notifica inviata:", response);
    return { success: true };
  } catch (error) {
    console.error("❌ Errore:", error);
    throw new functions.https.HttpsError("internal", error.message, data);
  }
});