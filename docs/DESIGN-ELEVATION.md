# Market / Everyday collection

## Design direction

The user’s reference is Nike: oversized display type, confident monochrome surfaces, large imagery, simple product cards and clear shopping actions. Market keeps its name and mixed electronics, clothing and home catalog. No Nike logo, assets or product claims are included.

- Bundled Archivo Black display font (SIL Open Font License in `src/main/resources/fonts/OFL.txt`). Body text uses the platform’s system font.
- A continuous charcoal-to-black campaign with the supplied headset cutout, an orange halo and sound-wave rings. “TUNE IN. STAND OUT.” is the headline. Explore headphones searches the Electronics collection for headphones; Browse collection scrolls to products; the artwork opens Electronics.
- Staggered banner entrance, floating headset, glow pulse, cursor parallax, button lift and product-image hover zoom. Reduce motion is available in the footer; animation pauses when the banner is offscreen or hidden.
- Visible search, collapsed filters, active filter chips, retained search/category/price/stock/sort and scroll state.
- Flat product cards with larger visual areas and consistent quick-add controls. Sold-out actions remain disabled.
- Collection shortcuts and an editorial closing section below the product grid.
- Shared styling for product details, cart drawer, cart, checkout, confirmation, orders, sign-in and inventory.
- Checkout scrolls delivery, payment and summary together so all controls are reachable in short windows. Choosing a payment clears stale validation feedback.

## Offline artwork

`CollectionArtwork` draws six original illustrations with JavaFX Canvas. Only exact demo names receive matching illustrations. Other products retain the neutral placeholder. Uploaded photos always take priority; the photo editor never substitutes an illustration for an absent upload. Illustrations are identified in accessibility text and on the product detail page as illustrations, with an “actual product may vary” caption. Database content and pricing are unchanged.

The campaign uses the user-supplied headset image, trimmed only to remove fully transparent margins and bundled under `src/main/resources/images/headset-campaign.png`. It does not overwrite catalog photos or imply that the wired headset is the catalog’s wireless model. The campaign is collection artwork, independent of current stock. Its action opens the category; it does not promise availability of a specific item.

## Verification

Java 21 and JavaFX 21.0.6 on Linux: 26 backend tests passed; the real JavaFX smoke harness passed 61 assertions and generated 24 snapshots, covering shopping, filtering, guest transfer, drawer interactions, declined/approved checkout, admin editing and upload persistence. The added checks cover visible search, campaign sizing/actions, category shortcuts and offline artwork. Screenshots are rendered at 1280×860 and 960×680; these do not replace presentation-machine checks of the native file picker, keyboard focus or display scaling.

The desktop application remains JavaFX. Start with `mvn compile javafx:run` or the existing Windows `build.ps1 Run` helper.

## Account power campaign and consistent product frames

Login uses the supplied PC cutout with “POWER UP. STAND OUT.” on a full-height charcoal panel. An orange halo, fine technical grid, corner marks and edition caption compose the artwork; drift, glow and parallax share the storefront banner lifecycle and session motion preference. The sign-in form has a Show/Hide password control, focused-field glow, its own Reduce motion checkbox and a scrollable container for shorter windows.

Catalog frames are square. Opaque uploads fill with a centered, proportional crop; transparent cutouts have alpha margins removed and fit within 88% of the frame, retaining the complete subject. Details and photo editor retain the full-image view. Center cropping can hide edges of unusually tall/wide opaque photos; use transparent product cutouts when the complete silhouette is essential. Image assignment remains controlled by the administrator: a headphone photo attached to “Mechanical keyboard” must be replaced with the correct product photo.

The MARKET navigation mark uses a 2 px orange pill outline with a soft stationary glow, slightly brighter on hover.

## Approved wireframe implementation

- The storefront headset hero is preserved. Only the login variant now fills 98% of its panel width with the supplied PC, clipped proportionally below the copy for the approved oversized composition.
- Product details show a larger full-image gallery, thumbnails only for actual uploaded angles, short description preview and optional real specifications. No alternate angles or hardware claims are fabricated.
- Admin editing supports four additional photos alongside the main photo and specifications (up to 1,000 characters). PNG/JPEG image validation applies to every gallery upload.
- Catalog quick-add confirms success briefly; a window-level toast includes Review cart. Active chips include Clear filters, and empty results offer the same recovery action.
- Larger cart item imagery, line amounts, stronger totals and orange purchase actions establish purchase hierarchy. Stock-limit plus controls disable in the cart and drawer; services still revalidate stock.
- Compact cart/checkout stack their columns and share one outer scroller. Checkout retains field values and focuses the first invalid field. The order action includes the actual total.
- Drawer focus cycles within the overlay and returns to the trigger, or the cart control when the toast trigger has disappeared. Existing reduced-motion, confirmation and order history behavior is retained.

Actual catalog photo assignments remain administrator-controlled. Correct photography and real additional angles/specifications must be uploaded for the relevant products; the existing offline demo illustrations remain available.
