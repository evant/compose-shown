# Compose-Shown

Provides a callback for when a `@Composible` is shown to the user. This can be useful for 'pageview'
analytics. It will re-fire when the activity is paused and resumed and _not_ re-fire on rotation.

## Usage

Simply add the `onShown` modifier to the composable you want to track.

```kotlin
Text("Hello", modifier = Modifier.onShown {
    // fire pageview analytics event
})
```

You can also optionally pass keys, this will cause it to re-fire when the key changes. Say, for
example, you want to re-send when the search query changes.

```kotlin
SearchResults(modifier = Modifier.onShown(query) {
    // fire when shown and when the query changes.                                                 
}, query = query)
```