package net.asfun.jangod.lib.tag;

import android.content.Context;

import net.asfun.jangod.interpret.InterpretException;
import net.asfun.jangod.interpret.JangodInterpreter;
import net.asfun.jangod.lib.Tag;
import net.asfun.jangod.tree.NodeList;
import net.asfun.jangod.util.HelperStringTokenizer;

/**
 * {% resTag 'strName' %}
 * {% resTag var_strName %}
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public abstract class AbsResTag implements Tag {

    protected static Context mContext;
    protected static String mPkgName;

    public static void init(Context context) {
        AbsResTag.mContext = context;
        mPkgName = context.getPackageName();
    }

    @Override
    public String interpreter(NodeList carries, String helpers, JangodInterpreter interpreter)
            throws InterpretException {
        String[] helper = new HelperStringTokenizer(helpers).allTokens();
        if (helper.length != 1) {
            throw new InterpretException("Tag '" + getName() + "' expects 1 helper >>> "
                    + helper.length);
        }
        String strName = interpreter.resolveString(helper[0]);
        return getValue(strName);
    }

    public abstract String getValue(String name);

    protected int getIdentifier(String name, String defType) {
        return mContext.getResources().getIdentifier(name, defType, mPkgName);
    }

}
