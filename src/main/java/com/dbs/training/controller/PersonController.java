package com.dbs.training.controller;

import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.dbs.training.exception.ObjectNotFound;
import com.dbs.training.model.Person;
import com.dbs.training.model.Role;
import com.dbs.training.service.PersonService;
import com.dbs.training.service.RoleService;
import com.dbs.training.util.Utils;
import com.dbs.training.validation.PersonValidator;

/**
 * Person Spring-MVC Controller.
 * 
 * @author John T Day
 * 
 */
@Controller
@RequestMapping(value = "/person")
public class PersonController {
	private static final Logger	logger					= Logger.getLogger(PersonController.class);
	private static final String	MESSAGE_FORMAT_SUCCESS	= "Person successfully %s <br/> %s";
	private static final String	MESSAGE_FORMAT_ERROR	= "Person error with %s <br/> %s";

	@Autowired
	private PersonService		personService;

	@Autowired
	private RoleService			roleService;

	@Autowired
	private PersonValidator		personValidator;

	@InitBinder
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(personValidator);
		binder.registerCustomEditor(Set.class, "roles", new CustomCollectionEditor(Set.class, true) {
			@Override
			protected Object convertElement(Object element) {
				Integer id = null;
				Role role = new Role();

				if (element instanceof String) {
					id = Integer.parseInt((String) element);
				} else if (element instanceof Integer) {
					id = (Integer) element;
				}
				role.setId(id);
				return id != null ? role : null;
			}
		});
	}

	@RequestMapping(value = "/tostring/{id}", method = RequestMethod.GET)
	public ModelAndView toStringPage(@PathVariable Integer id) {
		logger.debug("toStringPage: id=" + id);
		ModelAndView mav = new ModelAndView("object-toString");
		Person person = personService.findById(id);
		mav.addObject("object", person.toString());
		return mav;
	}

	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public ModelAndView newPersonPage() {
		logger.debug("newPersonPage: ");
		ModelAndView mav = new ModelAndView("person-new", "person", new Person());
		getDropDowns(mav);
		return mav;
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public ModelAndView createNewPerson(@ModelAttribute @Valid Person person, BindingResult result, final RedirectAttributes redirectAttributes) {
		logger.debug("createNewPerson: person=" + person);

		if (result.hasErrors())
			return getDropDowns(new ModelAndView("person-new"));

		ModelAndView mav = new ModelAndView("redirect:/index");
		String message = null;

		try {
			Person createdPerson = personService.create(person);
			message = String.format(MESSAGE_FORMAT_SUCCESS, "created", createdPerson.toString());
		} catch (Throwable e) {
			logger.error("createNewPerson: person=" + person, e);
			message = String.format(MESSAGE_FORMAT_ERROR, "created", Utils.unwindExceptionStackMessages(e));
		}
		redirectAttributes.addFlashAttribute("message", message);
		return mav;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public ModelAndView personListPage() {
		logger.debug("personListPage: ");
		ModelAndView mav = new ModelAndView("person-list");
		List<Person> personList = personService.findAll();
		mav.addObject("personList", personList);
		return mav;
	}

	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
	public ModelAndView editPersonPage(@PathVariable Integer id) {
		logger.debug("editPersonPage: id=" + id);
		ModelAndView mav = getDropDowns(new ModelAndView("person-edit"));
		Person person = personService.findById(id);
		mav.addObject("person", person);
		return mav;
	}

	@RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
	public ModelAndView editPerson(@ModelAttribute @Valid Person person, BindingResult result, @PathVariable Integer id,
			final RedirectAttributes redirectAttributes) throws ObjectNotFound {
		logger.debug("editPerson: person=" + person + ", id=" + id);

		if (result.hasErrors())
			return getDropDowns(new ModelAndView("person-edit"));

		ModelAndView mav = new ModelAndView("redirect:/index");
		String message = null;

		try {
			Person updatedPerson = personService.update(person);
			message = String.format(MESSAGE_FORMAT_SUCCESS, "updated", updatedPerson.toString());
		} catch (ObjectNotFound e) {
			logger.error("editPerson: Not person found with id=" + id);
			message = String.format(MESSAGE_FORMAT_ERROR, "updated", "Person not found with id=" + id);
		} catch (Throwable e) {
			logger.error("editPerson: person=" + person + ", id=" + id, e);
			message = String.format(MESSAGE_FORMAT_ERROR, "updated", Utils.unwindExceptionStackMessages(e));
		}
		redirectAttributes.addFlashAttribute("message", message);
		return mav;
	}

	@RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
	public ModelAndView deletePerson(@PathVariable Integer id, final RedirectAttributes redirectAttributes) throws ObjectNotFound {
		logger.debug("deletePerson: id=" + id);

		ModelAndView mav = new ModelAndView("redirect:/index");
		String message = null;

		try {
			Person personDeleted = personService.delete(id);
			message = String.format(MESSAGE_FORMAT_SUCCESS, "deleted", personDeleted.toString());
		} catch (ObjectNotFound e) {
			logger.error("deletePerson: Not person found with id=" + id);
			message = String.format(MESSAGE_FORMAT_ERROR, "delete", "Person not found with id=" + id);
		} catch (Throwable e) {
			logger.error("deletePerson: id=" + id, e);
			message = String.format(MESSAGE_FORMAT_ERROR, "delete", Utils.unwindExceptionStackMessages(e));
		}
		redirectAttributes.addFlashAttribute("message", message);
		return mav;
	}

	private ModelAndView getDropDowns(ModelAndView mav) {
		mav.addObject("roleList", roleService.findAll());
		return mav;
	}

}