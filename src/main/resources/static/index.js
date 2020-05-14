
// Web app's Firebase configuration
// Replace with the one given by Firebase after registering a web app
var firebaseConfig = {
    apiKey: "MyPuBlIc-ApIKeY",
    authDomain: "my-project-12345.firebaseapp.com",
    databaseURL: "https://my-project-12345.firebaseio.com",
    projectId: "my-project-12345",
    storageBucket: "my-project-12345.appspot.com",
    messagingSenderId: "xxxxxxxxxxxx",
    appId: "xxx...xxx"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
var database = firebase.database();
var clientUser = null;

//when our login authentication changes, update the user object
//and insert our data into the database
firebase.auth().onAuthStateChanged(
    function(user) {
        clientUser = user;
        userPing();
    }
);

//anonymous sign-in to obtain a UID from firebase
function userSignInAnonymous() {
    firebase.auth().signInAnonymously().catch(function(error) {
      var errorMessage = error.message;
      console.log(errorMessage);
    });
}


function userPing() {
    var username = '' + document.getElementById('username').value;
    var userrole = '' + document.getElementById('userrole').value;
    var date = '' + new Date();

    //update the last_seen date timestamp in '/users/uid' of our firebase
    database.ref('users/' + clientUser.uid).set({
        username: username,
        role: userrole,
        last_seen: date
    });
}


function loadMessages() {
    //continuously check for updates in the 'messages/' path of our firebase
    database.ref('messages/').on('value', function(snapshot) {
        var messageList = document.getElementById('message_list');
        var messageSegmentTemplate = document.getElementById('message_template').content;
        var messagesObject = snapshot.val();

        //reset list
        messageList.innerHTML = '';

        //refill list
        for (var sender in messagesObject) {
            //clone the template message box
            var messageSegment = messageSegmentTemplate.cloneNode(true);
            messageSegment.querySelector('.remove_button').id = sender;

            //set the message in the message box
            messageSegment.getElementById('message_content').innerHTML =
                "<b>" + sender + "</b>: " + messagesObject[sender];

            //insert new messagebox at the top of the list
            messageList.insertBefore(messageSegment, messageList.firstChild);
        }
    });
}


function sendMessage() {
    //grab the message from the input field
    var message =  document.getElementById('message_input').value;

    //insert a new message into the database with current date
    var newMessageRef = database.ref('messages/').push(new Date());

    //update the new message with the message from the input field
    newMessageRef.set(message);
}

function removeMessage(element) {
    database.ref('messages/').child(element.id).remove();
}