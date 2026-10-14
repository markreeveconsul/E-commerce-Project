# Approved next UI design

Implemented on the existing task branch. The storefront hero, PC campaign artwork, oversized crop, glow, grid, framing and banner motion are retained. A higher-quality PC image can replace `src/main/resources/images/login-pc-campaign.png` later. The login form now collapses demo shortcuts.

## Shopping screens

- Shared heart controls save products during a guest visit and persist them for customers. Guest selections merge on customer sign-in; signing out clears session state. Unavailable products are hidden from Saved items. Saving does not reserve inventory.
- Search suggestions use a 220 ms debounce and actual catalog matches. Stale requests cannot reopen a detached page. Custom minimum/maximum prices complement preset budgets; invalid ranges keep the previous results. Search, sorting, filters and scroll state survive navigation.
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

The existing Maven backend suite and new AccountAndRepeatTest cover 40 cases. GuiSmoke covers 89 shopping assertions; NextUiSmoke adds 35 assertions on a shown window, including saved-item transfer, profile/address persistence, search popup, price ranges, quantities, gallery zoom/focus, both Undo actions, saved-address checkout, payment cards, reviewed repeat orders, framing drafts/previews and compact layouts. BannerMotionSmoke covers 30 banner/motion assertions.

Run `mvn test` for backend checks. For the additional desktop harness, prepare the classpath with `mvn test-compile dependency:build-classpath -Dmdep.outputFile=target/classpath.txt`, then on Linux/macOS:

```bash
java -cp "target/test-classes:target/classes:$(cat target/classpath.txt)" edu.course.shop.NextUiSmoke /tmp/market-ui-screens target/next-ui-unique.db
```

Use a fresh database filename on every run. On a Linux host without a display, prefix the Java command with `xvfb-run -a` and add `-Dprism.order=sw`. The main Windows helper continues to run the existing GuiSmoke; NextUiSmoke can also be run as a Java main class in the IDE with its two arguments and test dependencies.
