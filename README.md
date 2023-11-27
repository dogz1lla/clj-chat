# clj-chat

## Description
A simplistic chat application to use in browser.

## Motivation
- I like clojure;
- I wanted to build something (potentially) useful;
- I wanted to build something using htmx;
- I wanted to build something using websockets;

## What's implemented?
- common chatroom for every user that is registered in the app ("`announcements`");
- separate chatrooms for each pair of users;
- automatic update of the chatroom list for every existing user upon a brand
new user connecting;
- randomly generated avatars using [avataaars](https://getavataaars.com/);

## What's NOT implemented?
- any auth -- anyone can impersonate anyone else;
- new message notifications;
- chatrooms of more than two users (only exception -- `announcements` chatroom);
- any db (atoms are used currently to hold all state);
- user defined avatars;

## How to run
Make your way to the root of the project and run
```
clj -M -m chatroom.main
```

## For further development
### Tailwind setup
Check out [this](https://youtu.be/V-dBmuRsW6w?si=1vh9GLk_zo9BH2Wv) video for useful info.
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
- each chatroom view updates the entire message history upon any new message;
should be updating the relevant div by only appending new messages;
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

## Notes
- html form's `onsubmit` didn't work well together with the HTMX's `ws-send`
attribute;
when i wanted to use a text input form for submitting a new message to the chat
and also wanted to clean the input form of the text (and use the `onsubmit` for
that to set its value to empty string) the message sent was empty;

## TODO
- [x] figure out why msg text is outside of the div;
- [x] make the chatbox expand automatically on new messages;
- [x] confirm that the message is sent from the ui to websocket;
- [x] update the html on receiving the msg over websocket;
- [x] add a "login" page on connect that simply asks for the username;
- [x] figure out how to pass the username from login to chatbox view;
- [x] setup the tailwind, see [here](https://tailwindcss.com/blog/standalone-cli)
- [x] create a chat button element and add the default one to the list;
- [x] write a README including how to setup tailwind css and how to run;
- [x] create a repo;
- [x] clear the input field after submitting the message
- [x] css: allow the chat message element to grow vertically if the message is
      too long;
- [x] do a proper chat autoscroll on new messages;
- [x] fix styling of the side buttons;
- [x] add a timestamp to every message;
- [x] fix styling of the "login" page;
- [x] cache avatar once the user joins the websocket;
- [x] hook up the chatroom view with the room logic, display rooms on the
      left of the view, let the user switch between them;
- [x] handle the case when an invalid username is provided; add htmx redirect;
- [x] add overfill auto to the side button column and test;
- [ ] figure out how to load htmx only once;
- [ ] update README with directions regarding running using clojure cli
building executable;
