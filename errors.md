# The Great List of Errors

This is a list of current and past errors. Please describe error and how it was resolved. Include the discovery data and rectification date.

## Current Errors

## Rectified Errors

`There was a service error: 403 : Access Not Configured. YouTube Data API has not been used in project 568626050628 before or it is disabled. Enable it by visiting https://console.developers.google.com/apis/api/youtube.googleapis.com/overview?project=568626050628 then retry. If you enabled this API recently, wait a few minutes for the action to propagate to our systems and retry.`
When this happens, the project needs to be deleted from the Google developer console, a new project needs to be created, the APIs need to be enabled, and then credentials need to be regenerated.

`WARNING: unable to change permissions for owner: C:\Users\admin\.oauth-credentials` [or a similar path]
Delete all tokens everywhere (e.g `/tokens`, `/.credentials`, and `[user home]/.credentials`). Make any code changes in Eclipse. And then refresh Eclipse. 


