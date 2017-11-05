package se.kodarkatten.casual.api.buffer.type;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public final class JavaServiceCallDefinition implements Serializable
{
    private static final long serialVersionUID = 1L;

    //Must reflect the name of the variable used for JSON serialization.
    public static final String METHOD_PARAMS = "methodParams";

    private String methodName;
    private String[] methodParamTypes;
    private Object[] methodParams;

    private JavaServiceCallDefinition( String methodName, String[] methodParamTypes, Object[] methodParams )
    {
        this.methodName = methodName;
        this.methodParamTypes = methodParamTypes;
        this.methodParams = methodParams;
    }

    public static JavaServiceCallDefinition of( String methodName, Object... params )
    {
        String[] paramTypes = new String[params.length];
        for( int i=0;i<params.length;i++ )
        {
            paramTypes[i] = params[i].getClass().getName();
        }
        return new JavaServiceCallDefinition( methodName, paramTypes, params );
    }

    public String getMethodName()
    {
        return this.methodName;
    }

    public String[] getMethodParamTypes()
    {
        return this.methodParamTypes;
    }

    public Object[] getMethodParams()
    {
        return this.methodParams;
    }
    public void setMethodParams(Object[] values )
    {
        this.methodParams = values;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        JavaServiceCallDefinition that = (JavaServiceCallDefinition) o;
        return Objects.equals(methodName, that.methodName) &&
                Arrays.equals(methodParamTypes, that.methodParamTypes) &&
                Arrays.equals(methodParams, that.methodParams);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(methodName, methodParamTypes, methodParams);
    }

    @Override
    public String toString()
    {
        return "JavaServiceCallDefinition{" +
                "methodName='" + methodName + '\'' +
                ", methodParamTypes=" + Arrays.toString(methodParamTypes) +
                ", methodParams=" + Arrays.toString(methodParams) +
                '}';
    }
}
