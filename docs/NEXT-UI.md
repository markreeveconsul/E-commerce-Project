# Approved next UI design

Implemented on the existing task branch. The storefront hero, PC campaign artwork, oversized crop, glow, grid, framing and banner motion are retained. A higher-quality PC image can replace `src/main/resources/images/login-pc-campaign.png` later. The login form now collapses demo shortcuts.

## Shopping screens

- Shared heart controls save products during a guest visit and persist them for customers. Guest selections merge on customer sign-in; signing out clears session state. Unavailable products are hidden from Saved items. Saving does not reserve inventory.
- Search suggestions use a 220 ms debounce and actual catalog matches. Stale requests cannot reopen a detached page. Custom minimum/maximum prices and preset budgets replace one another when applied; invalid ranges keep the previous results. Search, sorting, filters and scroll state survive navigation.
- Product details have breadcrumbs, a bounded minus/plus quantity control, useful optional content, real gallery angles, selected thumbnails, a short image transition and related-product cards. Secondary recommendations load without disabling the purchase controls.
- Enlarged photos offer pan, zoom, previous/next and keyboard arrows. Closing restores the gallery trigger's focus. No additional photographs or specifications are invented.
- Cards use brief lift/zoom effects and show an actual alternate angle on hover when available. Buttons retain visible keyboard focus and accessible stock states.
- Cart and drawer removals offer a six-second Undo. Restoration rechecks current price, activity and stock. Wide purchase summaries follow scrolling when they fit the viewport; compact layouts stack with normal scrolling.
- Checkout has payment choice cards, retained form input, optional saved-address selection, current totals and existing service-level validation. The demo-only decline option remains available for validation.
- Orders use cards with current catalog thumbnails and saved receipts. Buy again explains current prices and unavailable lines, asks for confirmation, and commits the rebuilt cart only after every available line validates. Receipts preserve purchase-time names and prices.

## Account and content tools

Customer profiles store a display name separately from the login username. Customers can save up to five validated delivery addresses and select a default. SQL operations scope all records to the signed-in customer; other users and roles cannot access or change them.

Admin gallery drafts support drag reordering, keyboard-accessible earlier/later actions and Make main. Framing can use AUTO, CONTAIN or COVER with a horizontal/vertical focal point. Preferences affect catalog images; original image bytes and full gallery photographs are retained. Preview photo frames compares catalog and full-image presentation. Preview product shows an unsaved draft. Structured specification rows and the optional text editor use the same saved content. The inventory flags missing photos, descriptions and specifications. Dimensions and file size help identify small source photos.

## Layout and motion

Compact headers use a Shop menu; product detail, account and admin columns stack at their content breakpoints. The compact product gallery caps its height so purchase controls require less scrolling. The shared palette and existing campaign CSS remain intact. Shopping pages use short fades; loading placeholders appear only for noticeable catalog waits. Reduced motion removes page fades, hover movement, alternate-angle transitions and existing campaign movement. Hidden/detached views stop their transient animations.

## Persistence

Additive migrations create `saved_items`, `customer_profiles` and `customer_addresses` with ownership foreign keys and a unique partial index for defaults. Product framing uses `photo_frame`, `photo_x` and `photo_y`; defaults preserve legacy automatic framing. Existing products, orders, galleries and users are retained. Gallery and framing saves remain transactional, including legacy stock-only edits.

## Scope and content

Product variants, fulfillment tracking, registration and account recovery were marked future work in the approved proposal and are not introduced here. Real catalog photography, benefits and specifications still need administrator-supplied content. No shipping estimates, rankings or delivery states are fabricated. Native file selection, manual gallery dragging and presentation-machine display/GPU verification remain manual.

## Verification

The existing Maven backend suite and new AccountAndRepeatTest cover 40 cases. GuiSmoke covers 89 shopping assertions; NextUiSmoke adds 59 assertions on a shown window, including saved-item transfer, profile/address persistence, search popup, price ranges, quantities, gallery zoom/focus, both Undo actions, saved-address checkout, payment cards, reviewed repeat orders, framing drafts/previews and verified 960×680 compact layouts. BannerMotionSmoke covers 30 banner/motion assertions.

Run `mvn test` for backend checks. For the additional desktop harness, prepare the classpath with `mvn test-compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`, then on Linux/macOS:

```bash
java -cp "target/test-classes:target/classes:$(cat target/classpath.txt)" edu.course.shop.NextUiSmoke /tmp/market-ui-screens target/next-ui-unique.db
```

Use a fresh database filename on every run. On a Linux host without a display, prefix the Java command with `xvfb-run -a` and add `-Dprism.order=sw`. The main Windows helper continues to run the existing GuiSmoke; NextUiSmoke can also be run as a Java main class in the IDE with its two arguments and test dependencies.

## Catalog and screen-access follow-up

The collection now has a full-width search surface with a visible clear action. Submitting or pressing Escape cancels pending suggestions. Clearing a search keeps category, budget and stock filters. Sort by has both Name A–Z and Name Z–A, plus both price directions, and survives detail navigation.

Filters have persistent labels, an active count, category, quick budget, custom USD range and availability. Applying a custom range clears the preset budget; choosing a budget clears the custom range. Invalid ranges retain previous results and focus the invalid input. Applied price ranges keep filtered results ahead of the campaign. The hero artwork, layout and motion are unchanged.

Saved items has a visible header label. Compact headers use the Shop menu to preserve room for account links. The product save control aligns with the category heading. Image dialogs load the shared stylesheet and support + / − to zoom, 0 or Fit image to reset, and arrows for real uploaded angles. Closing keeps the viewed angle and restores gallery focus. Inventory search/status/category controls have visible labels and a clear-search button. Account forms show field-specific errors and identify address editing mode.

### Where to open the reported screens

| Screen | How to open it |
|---|---|
| Account / address book | Sign in as customer → Profile |
| Saved items | Save a product with its heart → Saved items |
| Product details | Product card → View product |
| Image viewer | Product details → select the large image; uploaded photos support zoom/pan |
| Checkout | Add to cart → Cart → Checkout; customer sign-in required |
| Orders / receipt | Customer header → My orders; place a demo order to populate history |
| Admin editor | Sign out → sign in as admin → select inventory row → Edit product |

Gallery thumbnails require additional uploaded photos. Descriptions and specification panels appear when the administrator supplies that content. Guest saved items last for the session; customer items persist. Existing store data is retained by the additive migrations.

### Getting the complete update onto another computer

The latest work was committed locally. It is not available through `git pull origin main` until it has been published/merged on GitHub. A cumulative `market-ui-complete.bundle` is provided with this update; it contains earlier account/saved-item/gallery/checkout/order/editor changes and these refinements, including all required Git history.

Close the app, commit or stash any local edits, download the bundle into the project folder, and use PowerShell there:

```powershell
git switch main
git fetch ./market-ui-complete.bundle HEAD
git merge --no-edit FETCH_HEAD
.\build.ps1 Run
```

After checking the app, `git push origin main` publishes the merged update using your own laptop credentials, subject to your repository's branch rules. Do not delete `data/shop.db` or overwrite it with a test fixture. On other platforms use `mvn compile javafx:run` to rebuild and launch the current sources.
