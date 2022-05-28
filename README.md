# Crop
Crop for Android Jetpack Compose

Modified library from [Yalantis/uCrop](https://github.com/Yalantis/uCrop).

Accepts bitmap, results bitmap.

# Usage

```kotlin  
val cropView : CropView? = null

var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }

AndroidView(
    factory = {
        CropView(it)
    },
    modifier = Modifier.fillMaxSize(),
    update = { view ->
        cropView = view
        view.cropImageView.setImageBitmap(BITMAP)
        view.overlayView.frame = Frame()
        view.overlayView.grid = Grid()
        //...
    }
)

Button(onClick = {
    resultBitmap = cropView?.cropImageView?.crop()
}) {
    Text(text = "Crop")
}
```  
