# Market / Everyday collection

## Design direction

The user’s reference is Nike: oversized display type, confident monochrome surfaces, large imagery, simple product cards and clear shopping actions. Market keeps its name and mixed electronics, clothing and home catalog. No Nike logo, assets or product claims are included.

- Bundled Archivo Black display font (SIL Open Font License in `src/main/resources/fonts/OFL.txt`). Body text uses the platform’s system font.
- A dark campaign with a warm signal-orange illustration panel. Explore the collection scrolls to products; the artwork opens Electronics.
- Visible search, collapsed filters, active filter chips, retained search/category/price/stock/sort and scroll state.
- Flat product cards with larger visual areas and consistent quick-add controls. Sold-out actions remain disabled.
- Collection shortcuts and an editorial closing section below the product grid.
- Shared styling for product details, cart drawer, cart, checkout, confirmation, orders, sign-in and inventory.
- Checkout scrolls delivery, payment and summary together so all controls are reachable in short windows. Choosing a payment clears stale validation feedback.

## Offline artwork

`CollectionArtwork` draws six original illustrations with JavaFX Canvas. Only exact demo names receive matching illustrations. Other products retain the neutral placeholder. Uploaded photos always take priority; the photo editor never substitutes an illustration for an absent upload. Illustrations are identified in accessibility text and on the product detail page as illustrations, with an “actual product may vary” caption. Database content and pricing are unchanged.

The campaign is decorative collection artwork, independent of current stock. Its action opens the category; it does not promise availability of a specific item.

## Verification

Java 21 and JavaFX 21.0.6 on Linux: 26 backend tests passed; the real JavaFX smoke harness passed 61 assertions and generated 24 snapshots, covering shopping, filtering, guest transfer, drawer interactions, declined/approved checkout, admin editing and upload persistence. The added checks cover visible search, campaign sizing/actions, category shortcuts and offline artwork. Screenshots are rendered at 1280×860 and 960×680; these do not replace presentation-machine checks of the native file picker, keyboard focus or display scaling.

The desktop application remains JavaFX. Start with `mvn compile javafx:run` or the existing Windows `build.ps1 Run` helper.
