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

}
