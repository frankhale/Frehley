#Frehley

Frehley is a minimal text editor based on Node-Webkit and the Ace Editor library

##Changelog

9 May 2014

- Changed how "pages" are toggled, now a somewhat generic function performs the work
- Added start page and help page but they is nothing on them yet
- Changed the function key combos to: F1 - Start page, F2 - Control Panel, F3 - Cycle themes, F10 - Help
- Added a dependency for NeDB which is going to be used to store the recently edited files that
  will appear on the start page. This will also be used to store the app preferences and the JSON file
  that I'm using now will go away.

8 May 2014

- Theme toggling via F2 key
- Notification appears when theme is changed
- Title is updated with a * when a file has been edited
- When you drag a file onto the editor any empty default buffers (New.txt) will be closed
- CTRL+ALT+M closes all buffers
- Lots of tweaks and small changes

7 May 2014

- Fixed dragging and dropping multiple files on the editor

30 April 2014

- Added about page
- Added option to control panel to turn on line wrapping at 80 chars
- Added a fade in effect when transitioning between the editor and the control panel
- Some code cleanup

##Author

Frank Hale &lt;frankhale@gmail.com&gt;