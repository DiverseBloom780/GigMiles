# GigMiles

Android app for tracking DoorDash and Spark delivery miles, income, and business expenses.

## Initial product decisions

- GPS tracking replaces odometer entry.
- A drive belongs to one delivery app and cannot switch apps while active.
- Spark earnings: base pay, customer tips, boost, incentives.
- DoorDash earnings: DoorDash pay, customer tips.
- Historical work will support bulk totals rather than requiring one record per trip.
- Phone bills and other expenses will be recorded with business-use percentages.
- Tax exports will include CSV and a human-readable summary report.
- Maps will use the open-source MapLibre Android SDK with a replaceable OpenStreetMap-compatible tile source.
