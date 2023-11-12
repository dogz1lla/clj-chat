# clj-chat

## Short description
This is a messaging application.

## Motivation
I wanted to try and build something using htmx.

## Tailwind setup
- first of all, check out the stand-alone tailwind executable setup page:
[here](https://tailwindcss.com/blog/standalone-cli).
- don't forget to add `clj` file extension to `tailwind.config.js`; for more
details see this [guide](https://youtu.be/V-dBmuRsW6w?si=tNI89NMQvHnJAfg0&t=1954).
- create file `global.css` according to the above video guide.
- run the tailwind file watch:
```
./tailwindcss -i global.css -o resources/public/css/output.css --watch
```

## Design flaws
- There is a potential problem with how chat messages element gets updated on
receiving a new message.
How it works: one of the two parties in the chat sends a message and that
triggers the html element update for both the sender and recipient;
however for the latter only happens if the recipient looks at this same chat
in their browser at this moment;
Now imagine the situation where the recipient switches the view to another chat
after the update of the element responce was sent to the client from the server
but before the element was actually updated in the browser -> this would lead
to the user seeing the previous chat although they pressed a button to switch
to another one (minor inconvenience but still).

## TODO
- [x] figure out why msg text is outside of the div;
- [x] make the chatbox expand automatically on new messages;
- [x] confirm that the message is sent from the ui to websocket;
- [x] update the html on receiving the msg over websocket;
- [x] add a "login" page on connect that simply asks for the username;
- [x] figure out how to pass the username from login to chatbox view;
- [x] setup the tailwind, see [here](https://tailwindcss.com/blog/standalone-cli)
- [ ] create a chat button element and add the default one to the list;
- [x] write a README including how to setup tailwind css and how to run;
- [x] create a repo;
- [ ] clear the input field after submitting the message
- [ ] do a proper chat autoscroll on new messages;
- [x] cache avatar once the user joins the websocket;
- [ ] hook up the chatroom view with the room logic, display rooms on the
      left of the view, let the user switch between them;
