package model

object Types {
  // For now this is a pure marker type to indicate where we expect a weekday.
  // We could convert this into a proper class and add some validation
  // in the future or even introduce something like an enum.
  type Weekday = String
}
