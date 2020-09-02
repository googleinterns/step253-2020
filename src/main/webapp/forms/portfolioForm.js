/**
 * Handles setting up the portfolio form whenever the page is loaded.
 */
function setUpPortfolioForm() {
  addBlobstoreUploadUrlToForm("PORTFOLIO_FORM", "portfolioForm");
  styleInputs();
  activatePreviewFeature();
  fillPortfolioFormWithUserData();
}

function styleInputs() {
    const nameInput = new mdc.textField.MDCTextField(document.getElementById('nameInput'));
    const selfIntroductionInput = new mdc.textField.MDCTextField(document.getElementById('selfIntroductionInput'));
    const submitButtonRipple = new mdc.ripple.MDCRipple(document.getElementById("submitBtn"));
    const chooseFileButtonRipple = new mdc.ripple.MDCRipple(document.getElementById("chooseFileBtn"));
    const deletePrevImageCheckbox = new mdc.checkbox.MDCCheckbox(document.getElementById('deletePrevImageCheckbox'));
    const deletePrevImageFormField = new mdc.formField.MDCFormField(document.getElementById('deletePrevImageFormField'));
    deletePrevImageFormField.input = deletePrevImageCheckbox;
    const switchControl = new mdc.switchControl.MDCSwitch(document.getElementById("publicitySwitch"));
}

function activatePreviewFeature() {
  setSrcToElementOnChangeEvent("imgKey", "imagePreview", true);
}

/**
 * Gets the currently logged in user's data from the database
 * and fill's the form inputs with this data.
 */
function fillPortfolioFormWithUserData() {
  getUserDataFromServlet().then((user) => {
    const nameInput = new mdc.textField.MDCTextField(document.getElementById('nameInput'));
    const selfIntroductionInput = new mdc.textField.MDCTextField(document.getElementById('selfIntroductionInput'));
    setFormInputValue(nameInput, user.name);
    setFormInputValue(selfIntroductionInput,
        user.selfIntroduction);
    const switchControl = new mdc.switchControl.MDCSwitch(document.querySelector('.mdc-switch'));
    if (user.publicPortfolio) {
      switchControl.checked = true;
    } else {
      switchControl.checked = false;
    }
    if (user.imgKey != undefined) {
      setBlobKeySrcToElement(user.imgKey, "imagePreview", true);
    }
  });
}

/**
 * Gets the currently logged in user's data from the servlet.
 */
function getUserDataFromServlet() {
  return fetch('/user-data-servlet')
      .catch((error) => console.log('user-servlet: failed to fetch: ' + error))
      .then((response) => response.json())
      .catch((error) =>
        console.log(
            'fillFormInputsWithData: failed to convert to json: ' + error))
      .then((response) => {
        return response;
      });
}

