export interface PagesMenu {
    path: string; //菜单就随便写，路由就对应路由地址
    title: string;
    faPath: string[];
    icon?: string; //element的icon
    children?: PagesMenu[];
}

const menu: PagesMenu[] = [
    {
        path: "/pages/home",
        faPath: [],
        title: "主页",
        icon: "HomeFilled",
    },
    {
        path: "user",
        faPath: [],
        title: "系统管理",
        icon: "Menu",
        children: [
            {
                title: "路由查看",
                path: "/pages/systemManagement/routeView",
                faPath: ["user"],
            },
            {
                title: "用户管理",
                path: "/pages/systemManagement/user",
                faPath: ["user"],
            },
        ],
    },
    {
        path: "zhujian",
        faPath: [],
        title: "公共全局组件",
        icon: "Menu",
        children: [
            {
                title: "表格组件",
                path: "/pages/componentDemo/table",
                faPath: ["zhujian"],
            },
            {
                title: "表单组件",
                path: "/pages/componentDemo/form",
                faPath: ["zhujian"],
            },
        ],
    },
];

function setMenu(menu: PagesMenu[]) {
    menu.forEach((item) => {
        if (item.children) {
            item.children.forEach((child) => {
                child.faPath = item.path ? [item.path] : [];
                if (child.children) {
                    setMenu(child.children);
                }
            });
        }
    });
    return menu;
}

export default setMenu(menu);