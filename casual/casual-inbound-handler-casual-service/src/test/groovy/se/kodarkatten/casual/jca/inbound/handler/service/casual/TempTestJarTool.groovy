package se.kodarkatten.casual.jca.inbound.handler.service.casual

import se.kodarkatten.casual.jca.inbound.handler.service.casual.discovery.MethodMatcherTest

import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class TempTestJarTool
{
    /**
     * Create a temporary jar file containing the provided classes.
     * @param classes
     * @return jar file.
     */
    static File create( Class<?>... classes )
    {
        File jarFile = File.createTempFile( "tmp",".jar" )
        Manifest manifest = new Manifest()
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0")
        JarOutputStream stream = new JarOutputStream( new FileOutputStream( jarFile ), manifest )
        for( Class<?> c: classes )
        {
            String path = c.getName().replace('.', '/') + ".class"
            JarEntry entry = new JarEntry(path)
            stream.putNextEntry(entry)
            stream.write(MethodMatcherTest.getClassLoader().getResourceAsStream(path).getBytes())
            stream.closeEntry()
        }
        stream.close()
        return jarFile
    }
}
