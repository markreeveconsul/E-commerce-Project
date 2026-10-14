# Storefront and admin photo update — 13 October 2026

## Design references
- [Apple Store](https://www.apple.com/store): category navigation, clear product names and prices, focused product-detail views.
- [IKEA](https://www.ikea.com/us/en/): category discovery and useful product information.

These are interaction and layout references. Market keeps its own branding, colors, and JavaFX implementation. No reference-site photographs, logos, or product descriptions are bundled.

## Approved scope implemented
The application opens to a guest storefront. A header contains search, account access and cart count; a category row supports quick browsing. Customers can search, sort, inspect a product, choose quantities and build a temporary cart before signing in. Product details retain the previous search/category/sort when returning to results. The hero hides in short windows to keep product information reachable.

Signing in as a customer transfers the guest cart without silently replacing prices. A service checks for changed stock, availability and price and asks the customer to review the cart. Checkout revalidates everything inside its existing transaction. An admin cannot purchase; a guest cart is kept separately during an admin session and restored on sign-out.

Product details show the administrator's description and photo, price, availability, quantity and an add-to-cart action. No fictitious specifications, ratings, sale discounts or shipping status are invented. Checkout has recipient, street, city and postal-code fields, preserves the draft when navigating back, and shows order confirmation after success.

## Upload and change a product photo
1. Select Sign in and use admin / admin123.
2. Select a product and Edit selected, or Add product.
3. Select Upload photo and choose a PNG or JPEG.
4. Inspect the preview. You can upload another file or select Remove photo.
5. Save product to apply the photo, description and other edits together.

Cancel discards changes. Canceling the operating-system file picker leaves the current draft unchanged. Invalid uploads show an inline message and keep the prior photo. Products without photos show a neutral placeholder.

Photo limits: 5 MiB (shown as 5 MB in the UI), at most 4096 × 4096 pixels. The service checks the actual image format and decodes it, rather than trusting the extension.

## Why store photos in SQLite?
For this small offline course catalog, storing bounded photo bytes in a BLOB keeps product data and photos together. Moving or deleting the original upload does not affect the saved product. Replacing/removing the photo is part of the same SQL write as the product details. There are no absolute file paths to break on the presentation machine.

Tradeoff: photos increase database size and memory usage when loading products. This is appropriate for a small demo catalog; a large production store would use separate asset storage and thumbnails. Copy data/shop.db when backing up the store. Do not put personal data or uploaded photos into the source ZIP.

The schema migration adds description and photo columns to an existing products table; existing accounts, products and orders are retained. No sample photos are imported into the user's database.

## Explain the class responsibilities
- Product: immutable description and defensively copied photo bytes.
- ProductPhotos: file reading, format, size, dimension and decode validation.
- ProductPhotoEditor: chooser, preview and temporary photo draft.
- ProductService / ProductRepository: authorized save / SQL persistence.
- ProductArtwork: aspect-ratio-preserving photo rendering or placeholder.
- ProductDetailView / CatalogState: product screen / retained filter preferences.
- Guest: explicit browsing identity with no persisted account.
- CartService: transfer and review of cart snapshots.
- DeliveryAddress: delivery validation independent of JavaFX.
- ShopWindow: navigation and session state; no SQL.

All four existing OOP demonstrations remain: encapsulated models, the User hierarchy, PaymentMethod abstraction and overridden payment behavior.

## Verification boundary
See TESTING.md and the saved logs for automated results. Scene snapshots are from an isolated test database; the small image labeled TEST PHOTO is a synthetic upload test fixture, not a bundled product image. The native file-picker interaction and presentation-machine checks remain manual.
