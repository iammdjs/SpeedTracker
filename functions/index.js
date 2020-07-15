//const functions = require('firebase-functions');
//const functions = require('request-promise');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });



/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

const functions = require('firebase-functions');
const nodemailer = require('nodemailer');
const admin = require('firebase-admin');
admin.initializeApp();
let db = admin.firestore();

// Configure the email transport using the default SMTP transport and a GMail account.
// For Gmail, enable these:
// 1. https://www.google.com/settings/security/lesssecureapps
// 2. https://accounts.google.com/DisplayUnlockCaptcha
// For other types of transports such as Sendgrid see https://nodemailer.com/transports/
// TODO: Configure the `gmail.email` and `gmail.password` Google Cloud environment variables.
const gmailEmail = functions.config().gmail.email;
const gmailPassword = functions.config().gmail.password;
const mailTransport = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: gmailEmail,
    pass: gmailPassword,
  },
});

// Your company name to include in the emails
// TODO: Change this to your app or company name to customize the email sent.
const APP_NAME = 'Speed Tracker';

// [START sendWelcomeEmail]
/**
 * Sends a welcome email to new user.
 */
// [START onCreateTrigger]
exports.sendWelcomeEmail = functions.auth.user().onCreate((user) => {
// [END onCreateTrigger]
  // [START eventAttributes]
  const email = user.email; // The email of the user.
  //const displayName = user.displayName; // The display name of the user.
  // [END eventAttributes]

  //return sendWelcomeEmail(email, displayName);

  const userId = user.uid;

  let userData = db.collection('users').doc(userId);
  let getDoc = userData.get()
  .then(doc => {
  	if (!doc.exists) {
	  	console.log('No such document!:',userId);
	}
	else{
		//const email = doc.data().email;
		const displayName = doc.data().fullName;
	    sendWelcomeEmail(email, displayName);
	    console.log('Document data:', doc.data());
	}
    return null;
  })
  .catch((err) => {
    console.log('Error getting documents', err);
  });

  //const email = user.email;
  //const displayName = user.displayName;
  return null;
  ///sendGoodbyeEmail(email, displayName);
});
// [END sendWelcomeEmail]

/*exports.displayNameCreate = functions.firestore.document('/users/{uid}')
    .onCreate((snap, context) => {
// [END makeUppercaseTrigger]
      // [START makeUppercaseBody]
      // Grab the current value of what was written to Cloud Firestore.
      const email = snap.data().email;
      const displayName = snap.data().fullName;

      const userI = functions.auth.user.uid;

      console.log('User created : ',userI);

      return sendWelcomeEmail(email, displayName);
    });*/

// [START sendByeEmail]
/**
 * Send an account deleted email confirmation to users who delete their accounts.
 */
// [START onDeleteTrigger]
exports.sendByeEmail = functions.auth.user().onDelete((user) => {
// [END onDeleteTrigger]
  const email = user.email;
  //const displayName = user.displayName;

  //const email = functions.firestore.data('/users/{uid}').email;
  //const displayName = functions.firestore.data('/users/{uid}').displayName;

  const userId = user.uid;

  let userData = db.collection('users').doc(userId);
  let getDoc = userData.get()
  .then(doc => {
  	if (!doc.exists) {
	  	console.log('No such document!:',userId);
	}
	else{
		//const email = doc.data().email;
		const displayName = doc.data().fullName;
	    sendGoodbyeEmail(email, displayName);
	    console.log('Document data:', doc.data());
	}
    return null;
  })
  .catch((err) => {
    console.log('Error getting documents', err);
  });

  //const email = user.email;
  //const displayName = user.displayName;
  return null;
  ///sendGoodbyeEmail(email, displayName);
});
// [END sendByeEmail]


// Sends a welcome email to the given user.
async function sendWelcomeEmail(email, displayName) {
  const mailOptions = {
    from: `${APP_NAME} <noreply@firebase.com>`,
    to: email,
  };

  // The user subscribed to the newsletter.
  mailOptions.subject = `Welcome to ${APP_NAME}!`;
  mailOptions.text = `Hey ${displayName || ''}! Welcome to ${APP_NAME}. I hope you will enjoy our service.`;
  await mailTransport.sendMail(mailOptions);
  console.log('New welcome email sent to:', email);
  return null;
}

// Sends a goodbye email to the given user.
async function sendGoodbyeEmail(email, displayName) {
  const mailOptions = {
    from: `${APP_NAME} <noreply@firebase.com>`,
    to: email,
  };

  // The user unsubscribed to the newsletter.
  mailOptions.subject = `Bye!`;
  mailOptions.text = `Hey ${displayName || ''}!, We confirm that we have deleted your ${APP_NAME} account. \n\nThank you for using ${APP_NAME}.`;
  await mailTransport.sendMail(mailOptions);
  console.log('Account deletion confirmation email sent to:', email);
  return null;
}