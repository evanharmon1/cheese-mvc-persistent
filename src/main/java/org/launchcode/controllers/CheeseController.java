package org.launchcode.controllers;

import org.launchcode.models.Category;
import org.launchcode.models.Cheese;
import org.launchcode.models.Menu;
import org.launchcode.models.data.CheeseDao;
import org.launchcode.models.data.CategoryDao;
import org.launchcode.models.data.MenuDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by LaunchCode
 */
@Controller
@RequestMapping("cheese")
public class CheeseController {

    @Autowired
    private CheeseDao cheeseDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private MenuDao menuDao;

    // Request path: /cheese
    @RequestMapping(value = "")
    public String index(Model model) {

        model.addAttribute("cheeses", cheeseDao.findAll());
        model.addAttribute("title", "My Cheeses");

        return "cheese/index";
    }

    @RequestMapping(value = "add", method = RequestMethod.GET)
    public String displayAddCheeseForm(Model model) {
        model.addAttribute("title", "Add Cheese");
        model.addAttribute(new Cheese());
        model.addAttribute("categories", categoryDao.findAll());
        return "cheese/add";
    }

    @RequestMapping(value = "add", method = RequestMethod.POST)
    public String processAddCheeseForm(@ModelAttribute  @Valid Cheese newCheese,
                                       Errors errors, @RequestParam(value="categoryId", required=false) Integer categoryId, Model model) {

        if (errors.hasErrors() || categoryId == null) {
            model.addAttribute("title", "Add Cheese");
            model.addAttribute("categories", categoryDao.findAll());
            if (categoryId == null) {
                model.addAttribute("categoryError", "You must choose a category");
            }
            return "cheese/add";
        }

        Category cat = categoryDao.findOne(categoryId);
        newCheese.setCategory(cat);

        cheeseDao.save(newCheese);
        return "redirect:";
    }

    @RequestMapping(value = "remove", method = RequestMethod.GET)
    public String displayRemoveCheeseForm(Model model) {
        model.addAttribute("cheeses", cheeseDao.findAll());
        model.addAttribute("title", "Remove Cheese");
        return "cheese/remove";
    }

    @RequestMapping(value = "remove", method = RequestMethod.POST)
    public String processRemoveCheeseForm(@RequestParam int[] cheeseIds) {

        // Iterate through all menus for the cheese you want to remove and delete it from the menu before deleting the cheese itself
        Iterable<Menu> menus = menuDao.findAll();
        for (int cheeseId : cheeseIds) {
            for (Menu menu : menus) {
                if (menu.getCheeses().contains(cheeseDao.findOne(cheeseId))) {
                    menu.delCheese(cheeseDao.findOne(cheeseId));
                }
            }
            cheeseDao.delete(cheeseId);
        }

        return "redirect:";
    }

    @RequestMapping(value = "edit/{id}", method = RequestMethod.GET)
    public String displayEditForm(Model model, @PathVariable int id) {

        Cheese cheese = cheeseDao.findOne(id);
        model.addAttribute("cheese", cheese);
        model.addAttribute("title", "Edit Cheese " + cheese.getName() + " id = " + cheese.getId());
        model.addAttribute("categories", categoryDao.findAll());

        return "cheese/edit";
    }


    @RequestMapping(value = "edit", method = RequestMethod.POST)
    public String processEditForm(int cheeseId, String name, String description, int categoryId) {

        Cheese cheese = cheeseDao.findOne(cheeseId);
        cheese.setName(name);
        cheese.setDescription(description);
        cheese.setCategory(categoryDao.findOne(categoryId));
        cheeseDao.save(cheese);

        return "redirect:";
    }

    @RequestMapping(value = "category/{id}")
    public String category(@PathVariable int id, Model model) {

        Category category = categoryDao.findOne(id);
        List<Cheese> cheeses = category.getCheeses();
        model.addAttribute("title", category.getName());
        model.addAttribute("cheeses", cheeses);

        return "cheese/index";

    }

}
