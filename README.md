**ShoppingList** is a sample Android application that talks to an Azure Mobile Service. The cloud service handles data persistence, authentication, and push notifications. There are two versions of the app -- a "Starter" version that uses a local database, and a "Final" version that doesn't have any local storage anymore, and uses the cloud exclusively.

**iShoppy** is a rudimentary iOS version of the same application, which only demonstrates basic authentication and data query capabilities.

A possible extension is to integrate offline sync capabilities (offered by the Mobile Services client SDK) so that the app can work offline and push/pull changes when the device goes online.

The repo also contains my presentation from O'Reilly Software Architecture Conference in New York, April 2016. The presentation covered Azure Mobile Services and this app was used as a demo.
