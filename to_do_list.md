#The Great To-Do List

Stuff that needs to be done so that the app will do what it's supposed to do.

## Implement logic for...

+ Primary tasks
    1. Accessing user's email account. (done!)
    2. Parsing user's inbox. (done!)
    3. Finding youtube video emails. (done!)
    4. Adding YouTube video emails to list. (done!)
    5. Adding video URLs to list. (done!)
    6. Creating playlists. (done!)
    7. Adding videos to playlists.
    8. Creating playlists by different categories, eg date and author. Filter by date and author.
    9. Confirming that videos are in playlist.
    10. Deleting videos or sending them to trash.

+ Secondary tasks
    1. Deleting old token when user or gmail scope changes.
    2. A better entry point.
    3. One (overloaded?) method for authenticating both Gmail and YouTube HTTP requests.
    4. Logging.
    5. Test classes. (done!)
    6. Modify youtube and gmail method classes so that they process only youtube and gmail stuff. For example, the gmail class processes lists. That functionality should be in a distinct utility class.
    7. Reorganizes classes and packages.

## Fix errors

Check [errors](https://github.com/jmcart9/YouTubeProgram/blob/master/errors.md) for current errors.

## Other stuff

* Add non-English support

* Set up database. Database should contain user credentials with user id as the primary key. Needs to be encrypted.

* Create GUI.

* Find a better name for the app.

* Create a version of the application by using the MEAN stack.