package boa.graphs.cdg;

import boa.graphs.cfg.CFG;
import boa.types.Ast.*;


public class CDG {

    public CDG(final Method method) throws Exception {
        CFG cfg = new CFG(method);
        //postDominator(cfg);
        //dominatorFrontier();
        //constructCDG();
    }


}