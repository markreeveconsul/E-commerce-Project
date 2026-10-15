# Filter drawer and image viewer

Only the catalog filter interaction and enlarged photo viewer were changed. EditorialHeader.java and ShopWindow.java were byte-for-byte unchanged during this update. New CSS selectors are scoped to the new components.

- FilterDrawer owns draft category, availability and price selections. Apply commits them together; close, Escape and clicking outside discard the draft. Clear all resets the draft and preserves the search and sorting when applied.
- PriceRange renders one track and two non-crossing handles. Drag to adjust; arrow keys change by $1, Shift plus arrows by $10, Home/End move to the permitted bounds. Display and drag values retain cents. Limits come from catalog prices.
- ShopOverlay owns the modal backdrop, focus containment, dismissal and restoration of background interaction/effects. The image viewer uses a blurred background and a translucent charcoal surface. The image and controls remain sharp.
- ProductGallery keeps zoom and pan within a fixed viewing area. The viewer resizes with the app and retains the selected photo on close.

Verification: 43 unit tests; GuiSmoke 93 assertions; NextUiSmoke 81 assertions. GUI checks include draft cancellation, applying ranges, retaining search, keyboard adjustment, compact sizing, restoring focus/background effects and 12 image stability samples. Screenshots in docs/screenshots/overlay-review use isolated synthetic product data, not the real store database.