> Historical first UI refresh. The current storefront, guest flow and administrator photo controls are documented in [STOREFRONT-UPDATE.md](STOREFRONT-UPDATE.md).

# UI and UX refresh — October 13, 2026

## Reference research
Reviewed the public Apple Store, IKEA USA, and UNIQLO USA pages. UNIQLO exposed very little usable page content, so the concrete design references were Apple and IKEA.

- [Apple Store](https://www.apple.com/store): category-led navigation and product names/prices presented as clear browsing units.
- [IKEA](https://www.ikea.com/us/en/): product-category browsing and imagery supporting product discovery.
- [Nielsen Norman Group](https://www.nngroup.com/articles/ten-usability-heuristics/): visible status, error prevention, recovery, and consistent controls.

These informed the application design; no retailer branding, code, product photos, marketing claims, or promotional prices were copied. Original JavaFX Canvas illustrations render offline. Unknown product names receive a generic category/package illustration. This is not an image-upload feature or a promise of photographic accuracy.

## Implemented changes
- A warm neutral/green visual theme with readable category names and shared CSS.
- Responsive customer product cards with individual Add buttons; sold-out products show text and disabled actions.
- Search, category filtering, price sorting, and a Clear filters action.
- Active navigation state and cart count reflecting total units.
- Compact cart rows with per-item +/- controls, typed quantity updates (Enter or Update), removal, and a shared summary.
- Inline validation and success/error feedback; operation status during background work.
- Cart and order empty states with a browsing action.
- Checkout retains address and payment choice during same-session navigation.
- Order confirmation includes order ID, payment state, purchase lines, and next actions.
- Admin search, category/status filters, low-stock count, explicit create/edit mode, and field-level validation.
- Editor mode hides the product table while editing to keep the form prominent; Cancel discards the unsaved edit.
- Updated order receipt presentation and login screen.
- Default scene 1280×860; compact scene snapshots at 960×680. Scroll containers make overflow reachable.

## Class changes
New UI classes:
- ProductArtwork: local vector illustrations only.
- ProductCard: product presentation and delegated Add callback.
- OrderSummary: read-only cart totals and purchase lines.
- CheckoutDraft: temporary address/payment selection owned by ShopWindow.
- ConfirmationView: completed-order presentation.

Business models, services, SQL schema, and existing stored data do not require migration. Cart totals and transaction rules still come from existing models/services. Client-side checks improve feedback, but service/model checks remain authoritative.

The UML source and project guide have been updated. GuiSmoke now checks the revised controls and state transitions.

## Validation
17 backend tests passed before the visual review; final build results are recorded in TESTING.md.
Expanded GUI smoke: 22 assertions, covering invalid login, sold-out action, empty search/cart, clearing and sorting,
cart badge and quantity updates, invalid quantity, removal, address validation/retention,
declined and approved payment, confirmation, order history, admin creation, explicit editing, filtering, and deactivation.
Twelve scene snapshots cover seven screens, the admin editor, and compact layouts.

## Presentation talking points
ProductCard does not deduct stock. It delegates to a callback that calls CartService.
OrderSummary reads Cart.getTotal rather than implementing a second total formula.
CheckoutDraft represents unfinished screen input, so it belongs in the UI package and clears after purchase or logout.
The new UI demonstrates reuse through composition without adding an artificial inheritance hierarchy.

## Remaining manual checks
Test actual keyboard navigation and focus visibility on the presentation machine.
Check long product names and delivery addresses at your display scaling.
The screenshots are JavaFX scene renders; they are not evidence that every interactive usability scenario was manually tested.
