package org.groovydev;

import static org.junit.Assert.*;

import org.junit.Test;

class SimpleImageBuilderTest {

    @Test
    public void testSimpleImageBuilder() {
        SimpleImageBuilder b = new SimpleImageBuilder()
        def baos = new ByteArrayOutputStream()
        
        b.render(width:'100px',height:'100px') {
            fill(color:'ffff00')
            save(stream:baos, format:'png')
        }

        def bais = new ByteArrayInputStream(baos.toByteArray())
        def result = b.render(width:'100px',height:'100px') {
            fill(color:'ffffff')
            def small
            image(stream:bais) {
                small = fit(width:100,height:100)
            }
            draw(align:'center', image:small)
            save(stream:new ByteArrayOutputStream(), format:'png')
            fit(width:50,height:50) {
                save(stream:new ByteArrayOutputStream(), format:'png')
            }
            fit(width:20,height:20) {
                save(stream:new ByteArrayOutputStream(), format:'png')
            }
        }
    }

    @Test
    public void testSimpleImageBuilderFileJpg() {
        SimpleImageBuilder b = new SimpleImageBuilder()
        
        def result = b.render(width:'100px',height:'100px') {
           
           def small
           image(file:'test/files/apple-red.png') {
               small = fit(width:100,height:100)
           }
           draw(align:'center', image:small)
           fit(width:50,height:50) {
             save(file:'target/thumbnail-50p.jpg', format:'jpg', bgcolor:'00ff00')
           }
           fit(width:20,height:20) {
             save(file:'target/thumbnail-20p.jpg', format:'jpg')
           }

        }
    }

}
