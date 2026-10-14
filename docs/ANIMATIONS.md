# Interface animations

The UI owns motion. Services, inventory, cart totals and checkout transactions remain independent of animation timing.

## Headset campaign

`EditorialHeader` layers the supplied transparent headset over a charcoal gradient, orange radial glow and subtle sound-wave rings.

- Copy entrance: 460 ms fade/slide, staggered by 90 ms; product entrance: 650 ms fade/slide.
- Product float: 9 px over 3.2 seconds, reversing smoothly (6.4-second full cycle).
- Orange glow: opacity 0.55–0.88 over 3.8 seconds, reversing smoothly.
- Pointer parallax: up to 7 px horizontally and 4 px vertically, eased over 240 ms; leaving the banner centers the product.
- Moving the pointer is handled before child controls consume events, using scene coordinates converted into the banner's coordinates.

`CatalogView` reports whether the campaign intersects the scroll viewport. Banner motion pauses when offscreen, filtered out, disabled by a cart drawer/background operation, hidden or minimized. It resumes when visible again. Detaching it from its scene stops motion and removes listeners from the old window. Entrance and hover effects never delay navigation or cart actions.

## Hover feedback

`Motion` supplies reusable effects: buttons lift 2 px over 160 ms, and product-card visuals zoom to 1.035 over 220 ms. Card imagery stays clipped inside its frame. Disabled/detached controls reset their effects. Weak listeners prevent the global preference from retaining discarded controls.

## Reduce motion

The footer's **Reduce motion** checkbox retains the preference across screens for the current app session. It stops banner movement and makes the headline/product fully visible, resets pointer and hover transforms, and makes subsequent cart-drawer transitions immediate. Cart pulses are omitted; toast feedback is still shown and dismissed after its reading interval. Start with `-Dshop.reduceMotion=true` to default to reduced motion. No operating-system preference detection or cross-launch persistence is claimed.

## Existing cart feedback

`ShopWindow` retains its normal-motion interactions:

- Cart drawer: 200 ms slide with simultaneous backdrop fade.
- Cart count: 6% pulse lasting 180 ms total, when quantity increases.
- Added-to-cart message: 150 ms fade in, 2.2 seconds to read, 150 ms fade out.

A new pulse/message replaces the previous one. Navigation cancels pending drawer completion, and normal closing restores focus. Validation messages appear immediately.

## Verify

The regular `GuiSmoke` exercises the headphone action, motion toggle, navigation, shopping, checkout and uploads. `BannerMotionSmoke` uses a shown JavaFX window, real pointer movement and synthetic local products to check actual hover/float/glow/parallax, reduced motion, scene/window/viewport lifecycle, compact sizing, and scrolling inside the real storefront. It also exports actual scene frames for the animation preview. Both use software rendering under Xvfb on Linux; file-picker and presentation-machine scaling checks remain manual.

The account PC campaign reuses EditorialHeader's float, glow, entrance, pointer and window/detach lifecycle in a vertical composition. A login Reduce motion control updates the same session preference as the storefront footer. The logo glow is static. Photo zoom is clipped to the square catalog frame; image fitting does not stretch uploaded photos.

Catalog Add buttons hold an Added confirmation for 2.5 seconds, canceling the timer when detached. The cart confirmation toast offers Review cart and holds for four seconds; it is cleared on navigation and when opening the drawer. The approved larger PC uses the same existing drift/glow/parallax lifecycle with a proportional, clipped crop. Storefront hero animation settings remain unchanged.
