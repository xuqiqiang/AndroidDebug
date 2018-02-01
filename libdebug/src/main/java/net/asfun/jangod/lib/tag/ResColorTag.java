package net.asfun.jangod.lib.tag;

/**
 * {% rcolor 'strName' %}
 * {% rcolor var_strName %}
 * <p>
 * Created by xuqiqiang on 2017/04/17.
 */
public class ResColorTag extends AbsResTag {

    private final String TAGNAME = "rcolor";

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
        int id = getIdentifier(name, "color");
        return (String) mContext.getResources().getText(id);
    }

}
