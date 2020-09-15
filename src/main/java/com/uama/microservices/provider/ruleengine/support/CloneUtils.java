package com.uama.microservices.provider.ruleengine.support;

import java.io.*;

/**
 * @program: ulink
 * @description:
 * @author: liwen
 * @create: 2019-05-14 18:19
 **/
public class CloneUtils {
    
    private CloneUtils() {}
    
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T cloneWithSerialize(T obj) throws IOException, ClassNotFoundException {
        if (null == obj) {
            return null;
        }
        T clonedObj;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
    
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        clonedObj = (T) ois.readObject();
        ois.close();
        
        return clonedObj;
    }
}
