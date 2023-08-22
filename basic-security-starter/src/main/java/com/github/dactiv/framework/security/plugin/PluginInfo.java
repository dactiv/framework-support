package com.github.dactiv.framework.security.plugin;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.tree.Tree;
import com.github.dactiv.framework.security.entity.ResourceAuthority;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 插件信息
 *
 * @author maurice.chen
 */
public class PluginInfo extends ResourceAuthority implements Tree<String, PluginInfo> {

    
    private static final long serialVersionUID = -6354440242310314107L;

    /**
     * 默认版本号字段名称
     */
    public static String DEFAULT_VERSION_NAME = "version";

    /**
     * 默认插件字段名称
     */
    public static String DEFAULT_ARTIFACT_ID_NAME = "artifact-id";

    /**
     * 默认组字段名称
     */
    public static String DEFAULT_GROUP_ID_NAME = "group-id";

    /**
     * 默认的子节点字段名
     */
    public static final String DEFAULT_CHILDREN_NAME = "children";

    /**
     * 默认的来源字段名
     */
    public static final String DEFAULT_SOURCES_NAME = "sources";

    /**
     * 顶级父类表示
     */
    public static final String DEFAULT_ROOT_PARENT_NAME = "root";

    /**
     * id
     */
    private String id;

    /**
     * 图标名称
     */
    private String icon;

    /**
     * 顺序值
     */
    private Integer sort = 0;

    /**
     * 类型：参考 {@link ResourceType};
     */
    private String type;

    /**
     * 来源: 参考 {@link Plugin#sources()};
     */
    private List<String> sources;

    /**
     * 父类
     */
    private String parent;

    /**
     * 子节点
     */
    private List<Tree<String, PluginInfo>> children = new ArrayList<>();

    /**
     * 备注
     */
    private String remark;

    /**
     * 插件信息
     */
    public PluginInfo() {

    }

    /**
     * 插件信息
     *
     * @param plugin 插件信息
     */
    public PluginInfo(Plugin plugin) {
        this(plugin, new ArrayList<>());
    }

    /**
     * 插件信息
     *
     * @param plugin   插件信息
     * @param children 子节点
     */
    public PluginInfo(Plugin plugin, List<Tree<String, PluginInfo>> children) {
        this.setId(plugin.id());

        this.setParent(plugin.parent());
        this.setChildren(children);

        this.setName(plugin.name());
        this.setIcon(plugin.icon());
        this.setSort(plugin.sort());
        this.setType(plugin.type().toString());
        this.setSources(Arrays.asList(plugin.sources()));
        this.setRemark(plugin.remark());

        if (ArrayUtils.isNotEmpty(plugin.authority())) {
            this.setAuthority(StringUtils.join(plugin.authority()));
        }
    }

    /**
     * 获取 id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * 设置 id
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取图标
     *
     * @return 图标值
     */
    public String getIcon() {
        return icon;
    }

    /**
     * 设置图标
     *
     * @param icon 图标值
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * 设置父类 id
     *
     * @param parent 父类 id
     */
    public void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * 设置子节点
     *
     * @param children 子节点
     */
    public void setChildren(List<Tree<String, PluginInfo>> children) {
        this.children = children;
    }

    /**
     * 获取顺序值
     *
     * @return 顺序值
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置顺序值
     *
     * @param sort 顺序值
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }


    /**
     * 获取资源类型
     *
     * @return MENU.菜单类型, SECURITY.资源类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置资源类型
     *
     * @param type MENU.菜单类型, SECURITY.资源类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取来源集合
     *
     * @return 来源集合
     */
    public List<String> getSources() {
        return sources;
    }

    /**
     * 设置来源集合
     *
     * @param sources 来源集合
     */
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    /**
     * 设置备注
     *
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public List<Tree<String, PluginInfo>> getChildren() {
        return children;
    }

    @Override
    public String getParent() {
        return parent;
    }

    @Override
    public boolean isChildren(Tree<String, PluginInfo> parent) {
        PluginInfo pluginInfo = Casts.cast(parent);
        return StringUtils.equals(pluginInfo.getId(), this.parent);
    }
}
