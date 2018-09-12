const functions = require('firebase-functions');
const nodemailer=require('nodemailer');
const admin = require('firebase-admin');
admin.initializeApp();

//-------LETS WELCOME OUR ESTEEMED USER---------------------//
exports.sendWelcomeEmail = functions.auth.user().onCreate((user) => {
    const email = user.email; 
    
    
    // The email of the user.
if(!email){
    console.log('user signed up with phone '+user.phoneNumber);
    return ;
}


    let displayName = user.displayName;

    if(typeof displayName==undefined){
        console.log('user had no display name yet ');
        displayName="User";
    }
    
    
    // The display name of the user.
    let userMessage=`Hi,i notice you signed up for GetAplot, I just wanted to say thank you and let you know we are here to answer any questions.
    We genuinely value your feedback so please dont hesitate to contact with us at getaplotapp@gmail.com as one of the members will eventually get back to you or via twitter @getaplot even if its just a Hi!
    Cryce and the GetAplot team.
    Regards.
    `;
    let transporter = nodemailer.createTransport({
          service: 'gmail',
          auth: {
            user: 'crycetruly@gmail.com',
            pass: 'JXvq6thCuM!'
          }
        });

      // setup email data with unicode symbols
      let mailOptions = {
        from: `Cryce Truly  at GetAplot`, // sender address
        to: `${email}`, // list of receivers
        subject: 'Welcome to GetAplot', // Subject line
        text: `${userMessage}`, // plain text body
        html: `Hi,i noticed you signed up for <a href="https://www.getaplot.com" title"Events social app">GetAplot<strong></a>, I just wanted to say <strong>thank you</strong> and let you know we are here to answer any questions.
 <br>
        We genuinely value your feedback so please dont hesitate to contact with us at <a href="mailto: getaplotapp@gmail.com">getaplotapp@gmail.com</a>
         as one of the members will eventually get back to you or via twitter <a href="https://twitter.com/getaplot">@getaplot</a> even if its just a Hi!
         <br>
        Cryce and the GetAplot team.Regards.
        ` // html body
    };
    console.log(mailOptions);
    // send mail with defined transport object
    return transporter.sendMail(mailOptions, (error, info) => {
       if (error) {
           return console.log(error);
       }else{
           console.log('Email sent');
       }
       console.log('Message sent: %s', info.messageId); 
   
   });



});
//SEND NOTIFICATION ON REQUEST SENT/RECIEVED
exports.sendRequetsNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}')
.onWrite((event, context) => {
  const request=event.after.val();
  
  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a notification from : ', user_id);

  /*
   * HANDLE DELETES
   */

  if(!event.after.val()){

    return console.log('A Notification has been deleted from the database : ', notification_id);

  }

  /*
   * 'fromUser' query retreives the ID of the user who sent the notification
   */

  const fromUser = admin.database().ref(`/Notifications/${user_id}/${notification_id}`).once('value');

  return fromUser.then(fromUserResult => {

    const from_user_id = fromUserResult.val().from;

    console.log('You have new notification from  : ', from_user_id);

    /*
     * Use Firebase 'Promise'.
     * One to get the name of the user who sent the notification
     * another one to get the devicetoken to the device we want to send notification to
     */

    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();

      /*
       * We are creating a 'payload' to create a notification to be sent.
       */

      const payload = {
        notification: {
          title : `${userName}`,
          body: `${userName} has sent you a Friend Request`,
          icon: "default",
          sound: "default",
          click_action : "com.getaplot.asgetaplot.TARGET_NOTIFICATION"
        },
        data : {
          user_id :`${from_user_id}`
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('This was the notification Feature');

      });

    });

  });

});
 


  //accept


//------------------------TO RECIEVER----------------------
//SEND NOTIFICATION ON REQUEST SENT/RECIEVED
exports.sendAcceptNotification = functions.database.ref('/RequestAcceptNotifications/{user_id}/{notification_id}')
.onWrite((event, context) => {
    const request=event.after.val();
  /*
   STORE VALUES FROM DB AS VARIABLES
   */

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a notification from : ', user_id);

  /*
   * HANDLE DELETES
   */

  if(!event.after.val()){

    return console.log('A Notification has been deleted from the database : ', notification_id);

  }

  /*
   * 'fromUser' query retreives the ID of the user who sent the notification
   */

  const fromUser = admin.database().ref(`/RequestAcceptNotifications/${user_id}/${notification_id}`).once('value');

  return fromUser.then(fromUserResult => {

    const from_user_id = fromUserResult.val().from;
    const name = fromUserResult.val().name;

    console.log('You have new notification from  : ', from_user_id, 'to '+name);

    /*
     * Use Firebase 'Promise'.
     * One to get the name of the user who sent the notification
     * another one to get the devicetoken to the device we want to send notification to
     */

    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();

      /*
       * We are creating a 'payload' to create a notification to be sent.
       */

      const payload = {
        notification: {
          title : `${userName}`,
          body: `${userName} has added you back as a friend`,
         icon:'default',
          sound: "default",
          click_action : "com.getaplot.asgetaplot.TARGET_NOTIFICATION"
        },
        data : {
          user_id :`${from_user_id}`
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('This was Additions for friends');

      });

    });

  });

});

//new messsages

//SEND NOTIFICATION ON NEW MESSAGE RECIEVED
exports.sendNewMessageNotification = functions.database.ref('/MessageNotifications/{user_id}/{notification_id}').
onWrite((event, context) =>{


  /*
   STORE VALUES FROM DB AS VARIABLES
   */

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a notification from : ', user_id);

  /*
   * HANDLE DELETES
   */

  if(!event.after.val()){

    return console.log('A Notification has been deleted from the database : ', notification_id);

  }

  /*
   * 'fromUser' query retreives the ID of the user who sent the notification
   */

  const fromUser = admin.database().ref(`/MessageNotifications/${user_id}/${notification_id}`).once('value');

  return fromUser.then(fromUserResult => {

    const from_user_id = fromUserResult.val().from;
    let message = fromUserResult.val().message;

    let newmesso='';

    if(message.includes('firebasestorage.googleapis')){
      newmesso='aphoto';
    }else{
      newmesso=`${message}`;
    }

  



    console.log('You have new notification from  : ', from_user_id);

    /*
     * Use Firebase 'Promise'.
     * One to get the name of the user who sent the notification
     * another one to get the devicetoken to the device we want to send notification to
     */

    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const imageQuery = admin.database().ref(`Users/${from_user_id}/image`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');
    

    return Promise.all([userQuery, deviceToken,imageQuery]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();
      const image = result[2].val();

      /*
       * We are creating a 'payload' to create a notification to be sent.
       */

      const payload = {
        notification: {
          title : `${userName}`,
          body: `${newmesso}`,
          sound: "default",
          icon:`default`,
          click_action : "com.getaplot.asgetaplot.TARGET_MESSAGE_NOTIFICATION"
        },
        data : {

          user_id :`${from_user_id}`
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('This was the message notification Feature');

      });

    });

  });

});

//---------------------------LIKES NOTIFICATIONS----------------------------//


exports.sendLikesNotification = functions.database.ref('/LikeNotifications/{user_id}/{notification_id}').
onWrite((event,context) => {


  /*
   STORE VALUES FROM DB AS VARIABLES
   */

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a like from : to ', user_id);

  /*
   * HANDLE DELETES
   */

  if(!event.after.val()){

    return console.log('A Notification has been deleted from the database : ', notification_id);

  }

  /*
   * 'fromUser' query retreives the ID of the user who sent the notification
   */

  const fromUser = admin.database().ref(`/LikeNotifications/${user_id}/${notification_id}`).once('value');

  return fromUser.then(fromUserResult => {

    const from_user_id = fromUserResult.val().from;
    const commentId=fromUserResult.val().commentId;

    console.log('New Like from  : ', from_user_id);

    
     

    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();


      /*
       * We are creating a 'payload' to create a notification to be sent.
       */

      const payload = {
        notification: {
          title : "New Comment Like",
          body: `${userName} has liked your Comment`,
          icon: "default",
          sound: "default",
          click_action : "TARGET_LIKES_NOTIFICATION"
        },
        data : {
          commentId :`${commentId}`
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('Sending Like notification done');

      });

    });

  });

});
//comments 


//-------------------------send to commenters---------------------------------notifications

exports.sendCommentsNotifications=functions.database.ref('/CommentsNotifications/{event_id}')
.onWrite((event,context)=>{
  var request=event.after.val();
  var username=request.username;
  var event_name=request.name;
  var comment=request.text;
  
  let finalComment='';
  
      if(comment.includes('googleapis')){
        finalComment='Photo';
  
      }else{
        finalComment=`${comment}`;
      }
  
  console.log(finalComment);
  var payload={
   notification:{
     title:`${username}  on ${event_name}`,
     body:`${finalComment}`,
     sound: "default",
    icon:'default',
     click_action:'EVENT_TARGET__COMMENTS_NOTIFICATIONFOREVENT'
   },
    data:{
  event_id:request.event_id,
  event_name:request.name
    }
  }
  
  admin.messaging().sendToTopic(request.topic,payload).then(res=>{
  console.log('successfully send to topic '+request.topic);
  }).catch(err=>{
    console.log('An error has occured send to topic'+err);
  });
  });
  
  
//SEND NOTIFICATION ON REQUEST SENT/RECIEVED
exports.sendCommentRepliesNotification = functions.database.ref('/CommentReplyNotifications/{user_id}/{notification_id}').
onWrite((event,context) => {


  /*
   STORE VALUES FROM DB AS VARIABLES
   */

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a commentreply from : ', user_id);

  /*
   * HANDLE DELETES
   */

  if(!event.after.val()){

    return console.log('A Notification has been deleted from the database : ', notification_id);

  }

  /*
   * 'fromUser' query retreives the ID of the user who sent the notification
   */

  const fromUser = admin.database().ref(`/CommentReplyNotifications/${user_id}/${notification_id}`).once('value');

  return fromUser.then(fromUserResult => {

    const from_user_id = fromUserResult.val().from;
    const reply = fromUserResult.val().reply;
    const event_id=fromUserResult.val().event_id;
    const uid=fromUserResult.val().uid;
    const commentText=fromUserResult.val().commentText;
    const lastcommentedOn=fromUserResult.val().lastcommentedOn;
    
    const comment_id = fromUserResult.val().CommentId;

    console.log('You have new notification from  : ', from_user_id);

    /*
     * Use Firebase 'Promise'.
     * One to get the name of the user who sent the notification
     * another one to get the devicetoken to the device we want to send notification to
     */

    const userQuery = admin.database().ref(`Users/${from_user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/device_token`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();

      /*
       * We are creating a 'payload' to create a notification to be sent.
       */
//todo check first if this works
      const payload = {
        notification: {
          title : `${userName} Replied to your comment`,
          body: `${reply} `,
        icon: 'default',
          sound: "default",
          click_action : "NEW_REPLY_NOTIFICATION"
        },
        data : {
          CommentId :`${comment_id}`,
          event_id :`${event_id}`,
          uid :`${uid}`,
          commentText :`${commentText}`,
          lastcommentedOn :`${lastcommentedOn}`
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('This was the notification Feature');

      });

    });

  });

});

//-------------------------send to subscribers---------------------------------notifications

exports.sendEventAdditionNotifications=functions.database.ref('/EventsAddNotifications/{event_id}').onWrite(event=>{
  var request=event.data.val();
  var username=request.username;
  var event_name=request.name; 
  console.log(request);
  var payload={
   notification:{
     title:`${username} has added a new plot`,
     body:request.name,
     sound: "default",
     icon:'https://firebasestorage.googleapis.com/v0/b/getaplottestapp.appspot.com/o/str.png?alt=media&token=8e5592ae-373b-4f00-9975-db6d7630297b',
    click_action:'com.getaplot.asgetaplot.TARGET__COMMENTS_NOTIFICATION'
   },
    data:{
  event_id:request.event_id,
  event_name:request.name
    }
  }
  //todo just commented out the bug.
  // admin.messaging().sendToTopic( request.topic,payload).then(res=>{
  // console.log('successfully sevent addition sent to subscribers of topic '+request.username);
  // }).catch(err=>{
  //   console.log('An error has occured send to topic'+err);
  // });


  });
  
  
  //----------------------------------SEND TO INFO SUBSCRIBERS--------------------------//
  
  //-------------------------send to subscribers---------------------------------notifications
  
  exports.sendInfoNotifications=functions.database.ref('/InfoAddNotifications/{event_id}')
  .onWrite((event,context)=>{
    var request=event.after.val();
    var username=request.username;
  var event_name=request.name;
   var place_id=request.place_id;
    var post_id=request.post_id;
    console.log(request);
    var payload={
     notification:{
       title:"New Stories",
       body:`${username} has added a new post,check it now`,
      click_action:'com.getaplot.asgetaplot.NEWPOST_NOTIFICATION',
      sound: "default"
     },
      data:{
        place_id:place_id,
        post_id:post_id,
    event_name:request.name
      }
    }
    
    admin.messaging().sendToTopic(request.topic,payload).then(res=>{
    console.log('successfully send to subscribers of topic '+request.topic);
    }).catch(err=>{
      console.log('An error has occured send to topic'+err);
    });
    });
    
    //-------------------------send to subscribers---------------------------------notifications

    exports.sendEventAdditionNotifications=functions.database.ref('/EventsAddNotifications/{event_id}')
    .onWrite((event,context)=>{
    var request=event.after.val();
    var username=request.username;
    var event_name=request.name; 
    console.log(request);
    var payload={
     notification:{
       title:`${username} got a plot`,
       body:request.name,
       sound: "default",
      icon:'default',
      click_action:'com.getaplot.asgetaplot.TARGET__COMMENTS_NOTIFICATION'
     },
      data:{
    event_id:request.event_id,
    event_name:request.name
      }
    }
    
    admin.messaging().sendToTopic(request.topic,payload).then(res=>{
    console.log('successfully sevent addition sent to subscribers of topic '+request.username);
    }).catch(err=>{
      console.log('An error has occured send to topic'+err);
    });
    });
    
  
  
  //notify a place when i start floowing

  //------------------------TO RECIEVER----------------------
//SEND NOTIFICATION ON REQUEST SENT/RECIEVED
exports.sendFollowNotification = functions.database.ref('/placeNotifications/{place_id}/{notification_id}')
.onWrite((event, context) => {
    const request=event.after.val();
  /*
   STORE VALUES FROM DB AS VARIABLES
   */

  const place_id = context.params.place_id;
  const notification_id = context.params.notification_id;
  const user_id=request.who;

  console.log(user_id +'has followed '+place_id);

  /*
   * HANDLE DELETES
   */

  if(!event.after.val()){

    return console.log('A Notification has been deleted from the database : ', notification_id);

  }

  /*
   * 'fromUser' query retreives the ID of the user who sent the notification
   */

    const userQuery = admin.database().ref(`Users/${user_id}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${place_id}/device_token`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {

      const userName = result[0].val();
      const token_id = result[1].val();

      /*
       * We are creating a 'payload' to create a notification to be sent.
       */

      const payload = {
        notification: {
          title : `New follower`,
          body: `${userName} is now following your content`,
         icon:'default',
          sound: "default",
          click_action : "FOLLOWER_ACTIVITY"
        },
        data : {
          user_id :`${user_id}`
        }
      };

      /*
       * Then using admin.messaging() we are sending the payload notification to the token_id of
       * the device we retreived.
       */

      return admin.messaging().sendToDevice(token_id, payload).then(response => {

        console.log('This was Additions for friends');

      });

    });

  });
