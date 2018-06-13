const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.three_minutest_tick =
	functions.pubsub.topic('three-minutest-tick').onPublish((event) => {
		console.log("This job is ran every 3 minutes!")

		var ref = admin.database().ref(`/user_account_settings`);
		ref.once("value", function (snapshot) {
			var user = snapshot.val();
			snapshot.forEach(function (childSnapshot) {
				var deviceToken = childSnapshot.val().device_token;
				console.log(deviceToken);
				const message = {
					data: {
						title: "titlea",
						body: `body`,
						icon: "default"
					}
				};
				const options = {
					priority: `high`
				};
				admin.messaging().sendToDevice(deviceToken, message, options)
					.then(response => {
						return console.log('Notification send to ' + deviceToken);
					})
					.catch(err => { console.log(err) })
			});
			return;
		});
	});
