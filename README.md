# (Ace) Frehley

This is simple text editor using the Ace editor control in Node-Webkit. This is mainly
a learning aid in order to learn more about Clojurescript and Node-Webkit.

Editor Screenshot:

<img src="https://farm8.staticflickr.com/7366/13891522390_cc39f56a6e_b.jpg" alt="Editor Screenshot" />

Control Panel Screenshot:

<img src="https://farm3.staticflickr.com/2926/14078115835_b1f5f23b17_b.jpg" alt="Control Panel Screenshot" />

About Screenshot:

<img src="https://farm8.staticflickr.com/7068/13891494517_4996a176c0_b.jpg" alt="About Screenshot" />

## Usage

This editor must be ran inside of Node-Webkit

- Clone or download this repository, put the code in it's own folder named editor
- Download Node-Webkit from https://github.com/rogerwang/node-webkit
- Unzip Node-Webkit
- Copy the editor folder to your Node-Webkit folder
- Open a command prompt to the Node-Webkit directory
- Run the editor by typing the following into your command prompt and hitting enter: nw editor

Key short cuts

- CTRL+O - Opens the file open dialog
- CTRL+S - Saves file being edited or opens the file save as dialog
- CTRL+N - Creates new empty buffer
- CTRL+M - Closes the current file being edited
- CTRL+B - Reloads the editor in Node-Webkit
- CTRL+TAB - Cycles through opened files
- F1  - Opens the control panel (switch editor theme, font size and current buffer)
- F11 - Shows About information which also contains changelog data
- F12 - Opens Node-Webkit Dev Tools

You can drag files onto the editor and it will open the files for editing. Use CTRL+TAB to cycle through
them or hit F1 to open the control panel and select the file you want from the buffer list then hit F1 again
to return to the editor. (Yep, that's too many key strokes, this will be fixed!)

## Micellaneous

I'm using a fork of jayq for targetting jQuery from Clojurescript. The reason
for this that there are some compiler warnings with the latest version of the Clojurescript
compiler. I've already patched one warning and made a pull request that was accepted. As 
soon as there is a release of jayq that doesn't contain these warnings I'll start using that
instead. 

jayq:

https://github.com/ibdknox/jayq

## License

Copyright © 2014 Frank Hale

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
