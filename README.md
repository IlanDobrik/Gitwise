# Gitwise

## TODOs

* TransactionId
  * map[TransactionId]Transaction
  * Removes need to search transaction after edit
  * Also use as commit message
* Transaction class
  * Several owers

## . Generating a Personal Access Token (PAT)
To use GitWise, you must provide a GitHub Personal Access Token. This token acts as your password but allows you to limit what the app can do.

Follow these steps to create your token:

    Log in to your GitHub account.

    Click on your profile photo in the top-right corner and select Settings.

    On the left sidebar, scroll down and click Developer settings.

    Select Personal access tokens -> Tokens (classic).

    Click the Generate new token button (choose Generate new token (classic)).

    Note: Give your token a descriptive name (e.g., "GitWise Android App").

    Select Scopes: This is the most important step. To make the app work, you must check the following boxes:

        [x] repo (Full control of private repositories)

        [x] user (To read profile data/username)

    Scroll to the bottom and click Generate token.

    Copy the token immediately. You will not be able to see it again.