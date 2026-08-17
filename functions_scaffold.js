const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Triggered when a new participant is added to a challenge.
 * Sends a notification if the status is INVITED.
 */
exports.onChallengeInvite = functions.firestore
    .document('challenges/{challengeId}/participants/{participantId}')
    .onCreate(async (snap, context) => {
        const participant = snap.data();
        if (participant.status === 'INVITED') {
            const challengeSnap = await admin.firestore().collection('challenges').doc(context.params.challengeId).get();
            const challenge = challengeSnap.data();

            const userSnap = await admin.firestore().collection('users').doc(participant.userId).get();
            const user = userSnap.data();

            if (user.fcmToken) {
                const message = {
                    notification: {
                        title: 'New Challenge!',
                        body: `You've been invited to join "${challenge.title}"`
                    },
                    token: user.fcmToken
                };
                return admin.messaging().send(message);
            }
        }
    });

/**
 * Triggered when a participant accepts a challenge.
 */
exports.onChallengeAccept = functions.firestore
    .document('challenges/{challengeId}/participants/{participantId}')
    .onUpdate(async (change, context) => {
        const newValue = change.after.data();
        const oldValue = change.before.data();

        if (oldValue.status === 'INVITED' && newValue.status === 'ACCEPTED') {
            const challengeSnap = await admin.firestore().collection('challenges').doc(context.params.challengeId).get();
            const challenge = challengeSnap.data();

            // Notify creator
            const creatorSnap = await admin.firestore().collection('users').doc(challenge.createdBy).get();
            const creator = creatorSnap.data();

            if (creator.fcmToken) {
                return admin.messaging().send({
                    notification: {
                        title: 'Challenge Accepted',
                        body: `${newValue.userId} has joined your challenge "${challenge.title}"`
                    },
                    token: creator.fcmToken
                });
            }
        }
    });
