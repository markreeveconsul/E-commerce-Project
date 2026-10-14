# Interface animations

ShopWindow owns the animations; services and models remain independent of them.

- Cart drawer: a 200 ms slide with a simultaneous backdrop fade on opening and closing.
- Cart count: a 6% pulse lasting 180 ms in total, only when the quantity increases.
- Added-to-cart message: 150 ms fade in, 2.2 seconds to read, 150 ms fade out.

A new pulse or message replaces the previous animation. Repeated drawer close requests
are ignored while closing. Navigating away stops the drawer transition immediately,
so an old completion callback cannot change the destination screen. Closing normally
restores focus and refreshes the cart or checkout while retaining the delivery draft.

There are no looping animations or page-wide entrance effects. Existing validation
messages appear immediately. The GUI smoke checks cover normal Escape closing,
repeated closing during opening, and navigation during closing.