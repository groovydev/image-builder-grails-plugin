  A simple image builder.
  ======================
  
  Expressions:
  ------------
  
  render(width, height)            - render image
  draw(align,offsetX, offsetY,image) - draw image
  image(file|url|stream)           - load image
  save(file|stream,format,[image]) - save image
  scale(width,height)              - height is optional(use width) e.g: scale(50%) == scale(50%,50%)
  fit(width,height)                - relative scale the image until if fits (default as scale)
                                     bounds of the given box
  rotate(degrees,x,y)              - the rotation position x and y are optional (default is 50%)
  fill(color)                      - fill with color
  margin(left,top,right,bottom)    - add margins to image (resize image canvas)
 
  Values:
  values ending with '%' means size relative to image size.
  values ending with 'px' means values in absolute pixels.
  values without postfix use default notation.
  
 Example, how to create square 50px50p and 20px20p tumbnail images :
  
        def b = new org.groovydev.SimpleImageBuilder()
        def result = b.render(width:'100px',height:'100px') {
           fill(color:'ffffff')
           def small
           image(file:'image.jpg') {
             small = fit(width:100,height:100)
           }
           draw(align:'center', image:small)
           fit(width:50,height:50) {
             save(file:'thumbnail-50p.png', format:'png')
           }
           fit(width:20,height:20) {
             save(file:'thumbnail-20p.png', format:'png')
           }
        }
