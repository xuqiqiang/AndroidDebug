package net.asfun.jangod.lib.tag;

import net.asfun.jangod.interpret.InterpretException;
import net.asfun.jangod.interpret.JangodInterpreter;
import net.asfun.jangod.lib.Tag;
import net.asfun.jangod.tree.NodeList;

import java.util.UUID;

/**
 * {% uuid %}
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class UUIDTag implements Tag {

    private final String TAGNAME = "uuid";

    @Override
    public String getEndTagName() {
        return null;
    }

    @Override
    public String getName() {
        return TAGNAME;
    }

    @Override
    public String interpreter(NodeList carries, String helpers, JangodInterpreter interpreter)
            throws InterpretException {
        return UUID.randomUUID().toString();
    }

}
