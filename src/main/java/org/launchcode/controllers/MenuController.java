package org.launchcode.controllers;

import org.launchcode.models.Cheese;
import org.launchcode.models.Menu;
import org.launchcode.models.data.CheeseDao;
import org.launchcode.models.data.MenuDao;
import org.launchcode.models.forms.AddMenuItemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Controller
@RequestMapping(value = "menu")
public class MenuController {

    @Autowired
    private MenuDao menuDao;

    @Autowired
    private CheeseDao cheeseDao;

    @RequestMapping(value = "")
    public String index(Model model) {

        model.addAttribute("menus", menuDao.findAll());
        model.addAttribute("title", "My Menus");

        return "menu/index";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String add(Model model) {

        model.addAttribute(new Menu());
        model.addAttribute("title", "Add Menu");

        return "menu/add";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String add(Model model, @ModelAttribute @Valid Menu newMenu, Errors errors) {

        if (errors.hasErrors()) {
            model.addAttribute("title", "Add Menu");
            return "menu/add";
        }



        menuDao.save(newMenu);
        return "redirect:view/" + newMenu.getId();
    }

    @RequestMapping(value = "view/{id}", method = RequestMethod.GET)
    public String viewMenu(@PathVariable int id, Model model, @RequestParam(value = "duplicate", required=false) String errorMessage) {

        Menu menu = menuDao.findOne(id);
        model.addAttribute("menu", menu);
        model.addAttribute("errorMessage", errorMessage);

        return "menu/view";
    }

    @RequestMapping(value = "add-item/{id}", method = RequestMethod.GET)
    public String addItem(@PathVariable int id, Model model) {

        Menu menu = menuDao.findOne(id);
        AddMenuItemForm form = new AddMenuItemForm(menu, cheeseDao.findAll());
        model.addAttribute("form", form);
        model.addAttribute("title", "Add item to menu: " + menu.getName());

        return "menu/add-item";
    }

    @RequestMapping(value = "add-item", method = RequestMethod.POST)
    public String addItem(Model model, @ModelAttribute @Valid AddMenuItemForm form, Errors errors) {

        if (errors.hasErrors()) {
            return "menu/add-item";
        }

        Cheese cheese = cheeseDao.findOne(form.getCheeseId());
        Menu menu = menuDao.findOne(form.getMenuId());
        if (menu.getCheeses().contains(cheese)) {
            String errorMessage = "Menus can't contain duplicates";
            return "redirect:view/" + menu.getId() + "?duplicate=" + errorMessage;
        }
        menu.addItem(cheese);
        menuDao.save(menu);

        return "redirect:view/" + menu.getId();
    }


    @RequestMapping(value = "remove-item/{id}", method = RequestMethod.GET)
    public String removeItem(@PathVariable int id, Model model) {

        Menu menu = menuDao.findOne(id);
        model.addAttribute("cheeses", menu.getCheeses());
        model.addAttribute("title", "Remove cheese from menu: " + menu.getName());
        model.addAttribute("menuId", id);

        return "menu/remove-item";
    }


    @RequestMapping(value = "remove-item", method = RequestMethod.POST)
    public String processRemoveCategoryForm(@RequestParam int[] cheeseIds, Integer menuId) {

        Menu menu = menuDao.findOne(menuId);
        for (int cheeseId : cheeseIds) {
            Cheese cheese = cheeseDao.findOne(cheeseId);
            menu.delCheese(cheese);
        }

        menuDao.save(menu);

        return "redirect:view/" + menu.getId();

    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String displayRemoveMenuForm(Model model) {
        model.addAttribute("menus", menuDao.findAll());
        model.addAttribute("title", "Remove Menu");
        return "menu/remove";
    }

    @RequestMapping(value = "remove", method = RequestMethod.POST)
    public String processRemoveMenuForm(@RequestParam int[] menuIds) {
        for (int menuId : menuIds) {
            menuDao.delete(menuId);
        }

        return "redirect:";
    }

}

