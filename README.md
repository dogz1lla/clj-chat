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
