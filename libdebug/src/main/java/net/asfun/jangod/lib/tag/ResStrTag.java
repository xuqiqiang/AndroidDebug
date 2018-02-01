package net.asfun.jangod.lib.tag;

/**
 * {% rstr 'strName' %}
 * {% rstr var_strName %}
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class ResStrTag extends AbsResTag {

    private final String TAGNAME = "rstr";

    @Override
    public String getEndTagName() {
        return null;
    }

    @Override
    public String getName() {
        return TAGNAME;
    }

    @Override
    public String getValue(String name) {
        int id = getIdentifier(name, "string");
        return mContext.getString(id);
    }

}
