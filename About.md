# Frehley

Frehley is a minimal text editor based on NW.js and the Ace editor library

## Changelog

4 March 2018

* Update dependencies so that this works with the latest NW.js and Clojurescript
* Update Clojurescipt
* Update Ace
* Update README.md
* Update About.md

13 June 2014

* Removed some unfinished work that was going to go into creating a start
  screen.
* Removed dependency on hiccups, nedb for now until I have time to properly
  devote to this feature.
* Upgrade to latest Clojurescript

10 May 2014

* Add option to show/hide the print margin at 80 columns
* Fixed some other issues with properties being set correctly on the control
  panel when set in the config file.
* Add dependency on hiccups which will be used in the future for HTML element
  generation

9 May 2014

* Changed how "pages" are toggled, now a somewhat generic function performs the
  work
* Added start page and help page but they is nothing on them yet
* Changed the function key combos to: F1 - Start page, F2 - Control Panel,
  F3 - Cycle themes, F10 - Help
* Added a dependency for NeDB which is going to be used to store the recently
  edited files that will appear on the start page. This will also be used to
  store the app preferences and the JSON file that I'm using now will go away.

8 May 2014

* Theme toggling via F2 key
* Notification appears when theme is changed
* Title is updated with a \* when a file has been edited
* When you drag a file onto the editor any empty default buffers (New.txt) will
  be closed
* CTRL+ALT+M closes all buffers
* Lots of tweaks and small changes

7 May 2014

* Fixed dragging and dropping multiple files on the editor

30 April 2014

* Added about page
* Added option to control panel to turn on line wrapping at 80 chars
* Added a fade in effect when transitioning between the editor and the control
  panel
* Some code cleanup

## Author

Frank Hale &lt;frankhale@gmail.com&gt;
