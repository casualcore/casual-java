package se.kodarkatten.casual.jca.inflow.work;

import javax.resource.spi.work.HintsContext;
import javax.resource.spi.work.WorkContext;
import java.util.ArrayList;
import java.util.List;

public final class WorkContextFactory
{
    private WorkContextFactory()
    {

    }

    public static List<WorkContext> createLongRunningContext()
    {
        List<WorkContext> contexts = new ArrayList<>();

        HintsContext hintsContext = new HintsContext();
        hintsContext.setHint( HintsContext.LONGRUNNING_HINT, Boolean.TRUE );
        contexts.add( hintsContext );

        return contexts;
    }
}
