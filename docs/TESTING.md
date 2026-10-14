# Testing evidence

Updated and verified on October 13, 2026 (Asia/Singapore), using Windows and JDK 26.0.1.

## Automated backend tests
**25 tests passed; zero failures, errors, or skipped tests.**

ShopTest.java contains these executable JUnit cases:

| Test | Inputs/scenario | Expected result |
|---|---|---|
| loginChecksCredentialsAndResolvesRoles | Demo accounts; wrong/blank/SQL-like input | Correct subclass; invalid login rejected |
| invalidProductsCannotBeConstructed | Blank name, negative stock, nonnumeric/negative/overprecision price | ValidationException |
| customersCannotEditProductsEvenOutsideGui | Customer calls product save | Access denied |
| adminCreatesEditsAndDeactivatesProducts | New item -> edit -> deactivate | Stored correctly, hidden from customer |
| searchCombinesNameAndCategory | Uppercase name search and mismatched category | Case-insensitive match; correct filtering |
| cartCombinesItemsAndProtectsItsCollection | Add 1 then 2; update/remove; attempt list mutation | One line with 3; correct totals; collection protected |
| cartRejectsInvalidQuantitiesAndWrongOwner | Zero/excess quantity; wrong user | Rejected without valid cart changes |
| cashOnDeliveryCommitsOrderAndInventory | Buy 2 items | PENDING order; stock reduced by 2; cart cleared |
| approvedWalletUsesDifferentPaymentBehavior | Approved wallet | PAID order |
| declinedPaymentRollsBackStockAndKeepsCart | Declined wallet | No order; original stock/cart |
| insufficientSecondItemRollsBackFirstDeduction | Second item's stock reduced after cart addition | First deduction undone; no order |
| databaseWriteFailureRollsBackEntireCheckout | SQLite trigger aborts order insertion | StorageException; original stock/cart |
| checkoutRejectsEmptyCartAddressAndMissingPayment | Empty cart, blank address, null payment | ValidationException; no order |
| stalePriceRequiresCustomerReview | Price edited after cart addition | Checkout rejected; quantity update refreshes price |
| deactivatedCartItemCannotBePurchased | Product deactivated after cart addition | Checkout rejected; stock unchanged |
| orderSnapshotsSurviveProductEditsAndReopeningDatabase | Buy, change name/price, reopen file | Receipt retains original values; changes persist |
| historyDoesNotExposeOtherCustomersOrders | Different customer ID; admin | Other customer's orders absent; admin denied |
| photoAndDescriptionSurviveReopenReplacementAndRemoval | Upload PNG, delete original, reopen DB, replace with JPEG, remove | Persistent bytes/description; latest photo or null |
| invalidPhotosCannotReplaceSavedData | Corrupt, empty, oversize, excessive dimensions, GIF, fake JPEG, missing file | Reject; existing saved product remains intact |
| photoBytesAreEncapsulatedAndBasicEditPreservesPhoto | Mutate source/getter arrays; save stock-only edit | Defensive copies; photo and description retained |
| photosAndDescriptionsRequireAdminAccessAndValidDescription | Customer/guest save; overlong description | Service validation denies changes |
| existingSchemaMigratesWithoutLosingProducts | Old product table, migrate twice | Existing product intact; new columns available |
| guestCanBrowseAndBuildCartButCannotCheckoutOrReadOrders | Guest adds items, transfers to customer, attempts restricted operations | Cart transferred; checkout/history require customer |
| guestTransferNeverSilentlyChangesPriceAndCheckoutRevalidates | Admin changes price before guest sign-in | Original price retained; review warning; checkout requires update |
| deliveryDetailsAreValidatedOutsideTheGui | Missing/invalid fields, excessive combined length | DeliveryAddress rejects independently of GUI |

Run: `./build.ps1 Test`. Detailed Maven reports are generated under target/surefire-reports.

## GUI smoke test
**Passed: 35 assertions.** Fires actual JavaFX controls for inline login validation, catalog cards, sold-out actions, no-result searches, clear filters, price sorting, empty cart, cart counts, quantity edits, invalid quantity, removal, address validation/retention, declined and approved wallet checkout, confirmation, order history, and admin create/edit/filter/deactivation.

Additional checks cover guest browsing, product details, quantity preservation through sign-in, required delivery fields, photo import/preview/persistence, reopening the editor, canceling removal, corrupt uploads, and saving removal. The upload test calls the same import method used after file selection; the native chooser itself remains a manual check.

Run: `./build.ps1 Smoke`. Uses a unique test database under target and does not touch data/shop.db.

Seventeen actual JavaFX scene snapshots were generated. Main screens, upload editing and compact layouts were visually reviewed:

- 01-login.png
- 02-catalog.png
- 03-cart.png
- 04-checkout.png
- 05-orders.png
- 06-admin.png
- 07-confirmation.png
- 08-catalog-compact.png
- 09-cart-compact.png
- 10-checkout-compact.png
- 11-admin-editor.png
- 12-admin-compact.png
- 13-guest-storefront.png
- 14-product-detail.png
- 15-product-detail-compact.png
- 16-photo-upload.png
- 17-admin-editor-compact.png

These are application scene renders, not desktop captures or mockups. The smoke test does not replace an interactive usability review on the presentation machine.

## Build
Maven package completed successfully and produced target/marketplace-1.0.0.jar. This is a standard dependency-requiring JAR, not a standalone executable distribution.

## Manual checks still to perform personally
- [ ] Open the native photo picker; select PNG/JPEG, cancel selection, replace and remove a photo.
- [ ] Run build.ps1 Run on the presentation machine.
- [ ] Use Tab/Enter to log in and navigate form controls.
- [ ] Resize the window and verify tables/forms at your display scaling.
- [ ] Enter letters, fractions, huge numbers, and blanks in numeric inputs.
- [ ] Observe error dialogs for excess stock and declined demo payment.
- [ ] Log out with a nonempty cart; test both confirmation choices.
- [ ] Close and reopen the actual app; inspect saved orders and stock.
- [ ] Explain one test's setup, input, expected result, and assertion.

Do not mark these manual checks completed merely because the automated suite passes.
