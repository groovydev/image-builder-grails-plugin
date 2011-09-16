package org.groovydev

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.util.Map;

import javax.imageio.ImageIO

/**
 * A simple image builder.
 * 
 * Expressions:
 * 
 * render(width, height)            - render image
 * draw(align,offsetX, offsetY,image) - draw image
 * image(file|url|stream)           - load image
 * save(file|stream,format,[image]) - save image
 * scale(width,height)              - height is optional(use width) e.g: scale(50%) == scale(50%,50%)
 * fit(width,height)                - relative scale the image until if fits (default as scale)
 *                                    bounds of the given box
 * rotate(degrees,x,y)              - the rotation position x and y are optional (default is 50%)
 * fill(color)                      - fill with color
 * margin(left,top,right,bottom)    - add margins to image (resize image canvas)
 *
 * Values:
 * values ending with '%' means size relative to image size.
 * values ending with 'px' means values in absolute pixels.
 * values without postfix use default notation.
 * 
 * FIX: BufferedImage.TYPE_INT_ARGB writes black jpg image
 *  
 * Example, how to create square tumbnail image :
 * 
 *       SimpleImageBuilder b = new SimpleImageBuilder()
 *       def result = b.render(width:'100px',height:'100px') {
 *          fill(color:'ffffff')
 *          def small
 *          image(file:'image.jpg') {
 *            small = fit(width:100,height:100)
 *          }
 *          draw(align:'center', image:small)
 *          fit(width:50,height:50) {
 *            save(file:'thumbnail-50p.png', format:'png')
 *          }
 *          fit(width:20,height:20) {
 *            save(file:'thumbnail-20p.png', format:'png')
 *          }
 *       }
 * 
 * Code fragments based on code Philip Van Bogaert alias tbone.
 * Source: http://groovy.codehaus.org/Batch+Image+Manipulation
 * 
 * @author Karol Balejko <kb@groovydev.org>
 *
 */
class SimpleImageBuilder extends BuilderSupport {

    BuilderHelper helper
    
    public SimpleImageBuilder() {
        super()
        helper = new BuilderHelper()
    }

    protected void setParent(Object parent, Object child) {
    }

    protected Object createNode(Object name) {
        createNode name, null, null
    }

    protected Object createNode(Object name, Object value) {
        createNode name, null, value
    }

    protected Object createNode(Object name, Map attrs) {
        createNode name, attrs, null
    }

    protected Object createNode(Object name, Map attrs, Object value) {
        helper.current = current
        def node = helper[name].call(attrs)
        return node
    }

    protected Object postNodeCompletion(Object parent, Object node) {
        return node;
    }
    
    class BuilderHelper {
        
        def current
        
        def image = {Map attrs->//file,url,stream
            if (attrs.file) {
                def file = parseFile(attrs.file)
                ImageIO.read(file)
            } else if (attrs.url) {
                def url = parseUrl(attrs.url)
                ImageIO.read(url)
            } else if(attrs.stream) {
                ImageIO.read(attrs.stream)
            }
        }
        
        def save = {Map attrs->//[image=current],file,stream,format
            def image = attrs.image ?: current
            def format = parseFormat(attrs.format)
            if (attrs.file) {
                def file = parseFile(attrs.file)
                ImageIO.write(image, format, file)
            } else if(attrs.stream) {
                ImageIO.write(image, format, attrs.stream)
            }
            return image
        }
            
        def draw = {Map attrs->//align,offsetX, offsetY,image
            def imageBack = current
            def imageFront = attrs.image
            def parsedOffsetX = parseValue(attrs.offsetX, imageBack.width, true, 0)
            def parsedOffsetY = parseValue(attrs.offsetY, imageBack.height, true, 0)
            if (attrs.align == 'center') {
                Graphics2D g2 = imageBack.createGraphics()
                int canvasOffsetX = (imageBack.width - imageFront.width) / 2 + parsedOffsetX
                int canvasOffsetY = (imageBack.height - imageFront.height) / 2 + parsedOffsetY
                def result = g2.drawImage(imageFront, canvasOffsetX, canvasOffsetY, imageFront.width, imageFront.height, null)
                imageBack
            } else {
                throw new IllegalArgumentException('not supported align attribute [$attrs.align]')
            }
        }
    
        def render = {Map attrs->// width, height
            def image = current
            def parsedwidth =  parseValue(attrs.width, image?.width, true, image?.width);
            def parsedHeight =  parseValue(attrs.height, image?.height, true, image?.height);
            def newImage = new BufferedImage(parsedwidth, parsedHeight, BufferedImage.TYPE_INT_RGB);
            return newImage
        }
            
        def fill = {Map attrs->//color
            def image = current
            def color = parseColor(attrs.color)
            def graph = image.createGraphics()
            graph.setPaint(color)
            graph.fillRect(0, 0, image.width, image.height)
            current
        }
    
        def rotate = {Map attrs->//degrees,x,y
            def image = current
            def parsedRadians = 0;
            try {
                parsedRadians = Math.toRadians(Double.parseDouble(attrs.degrees));
            } catch(NumberFormatException e) {
                throw new IllegalArgumentException("command rotate", e)
            }
    
            def parsedX = parseValue(attrs.x, image.width, true, "50%")
            def parsedY = parseValue(attrs.y, image.height, true, parsedX)
    
            rotate(image,parsedRadians, parsedX, parsedY)
        }
    
        def rotate(image,radians,x,y) {
            def transform = new AffineTransform();
            transform.rotate(radians,x,y);
            def op = new AffineTransformOp(transform,AffineTransformOp.TYPE_BILINEAR);
            def newImage = op.filter(image,null);
            return newImage
        }
    
        def scale = {Map attrs->//width, height
            def image = current
            def parsedHorizontal =  parseValue(attrs.width,image.width,false,"100%");
            def parsedVertical =  parseValue(attrs.height,image.height,false,parsedHorizontal);
            scale(image,parsedHorizontal,parsedVertical)
        }
    
        def scale(image, horizontal,vertical) {
            def transform = new AffineTransform();
            transform.scale(horizontal,vertical);
            def op = new AffineTransformOp(transform,AffineTransformOp.TYPE_BILINEAR);
            def newImage = op.filter(image,null);
            return newImage
        }
    
        def margin =  {Map attrs->// left,top,right,bottom
            def image = current
            def parsedLeft = parseValue(attrs.left,image.width,true,"0px");
            def parsedTop =  parseValue(attrs.top,image.height,true,parsedLeft);
            def parsedRight = parseValue(attrs.right,image.width,true,parsedLeft);
            def parsedBottom = parseValue(attrs.bottom,image.height,true,parsedTop);
            def newImage = margin(parsedLeft,parsedTop,parsedRight,parsedBottom);
            return newImage
        }
    
        def margin(image,left,top,right,bottom) {
            def width = left + image.width + right;
            def height = top + image.height + bottom;
            def newImage = new BufferedImage(width.intValue(), height.intValue(),BufferedImage.TYPE_INT_ARGB);
            def graph = newImage.createGraphics();
            graph.drawImage(image,new AffineTransform(1.0d,0.0d,0.0d,1.0d,left,top),null);
            return newImage
        }
    
        def fit = {Map attrs->// width,height
            def image = current
            def parsedWidth = parseValue(attrs.width,image.width,true,"100%");
            def parsedHeight = parseValue(attrs.height,image.height,true,parsedWidth);
    
            def imageRatio = image.width / image.height;
            def fitRatio = parsedWidth / parsedHeight;
    
            if(fitRatio < imageRatio) {
                parsedHeight = image.height * (parsedWidth/image.width)
            } else {
                parsedWidth = image.width * (parsedHeight/image.height)
            }
            scale.call(width:"${parsedWidth}px", height:"${parsedHeight}px")
        }
    
        Color parseColor(value,defaultValue="0xffffff") {
    
            def pattern = "(0x)?([0-9a-fA-F]+\\.?[0-9a-fA-F]*)(.*)";
            def matcher = value =~ pattern;
            if(!matcher.find()) {
                matcher = defaultValue =~ pattern;
                matcher.find();
            }
    
            def decimalValue
            def type = matcher.group(1)
            if (type == '0x') {
                decimalValue = Integer.parseInt(matcher.group(2), 16)
            } else {
                decimalValue = Integer.parseInt(matcher.group(2), 16)
            }
    
            return new Color(decimalValue)
        }
        
        /**
         * absolute true  -> returns pixels.
         *          false -> returns relative decimal (e.g 1.0).
         */
        Number parseValue(value,size,absolute,defaultValue = '0') {
            def pattern = "(-?[0-9]+\\.?[0-9]*)(.*)";
            def matcher = value =~ pattern;
            if(!matcher.find()) {
                matcher = defaultValue =~ pattern;
                matcher.find();
            }
    
            def decimalValue = Double.parseDouble(matcher.group(1));
            def type = matcher.group(2);
    
            if(absolute) { // pixels
                switch(type)  {
                    case "%":
                        return (int) size * (decimalValue / 100);
                    case "px":
                    default:
                    return (int) decimalValue;
                }
            }
            else { // scale
                switch(type) {
                    case "px":
                        return decimalValue / size;
                    case "%":
                        return decimalValue / 100;
                    default:
                        return decimalValue;
                }
            }
        }
    
        String parseFormat(String formatName) {
            def writerNames = ImageIO.getWriterFormatNames()
            if (!writerNames.any{it == formatName}) {
                throw new IllegalArgumentException("$formatName is not supported format $writerNames")
            }
            return formatName
        }

        def parseUrl(url) {
            if (url instanceof String) {
                url = new URL(url)
            }
            return url
         }
         
         def parseFile(file) {
             if (file instanceof String) {
                 file = new File(file)
             }
             return file
         }
 
    }
}
