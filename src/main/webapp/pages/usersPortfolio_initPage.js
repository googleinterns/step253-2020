/**
 * This function initializes the usersPortfolio page
 * having the following components(for a selected user A):
 * - one map showing the guides of the user A in the map area
 * - a list displaying the same guides
 * - an info box with user A's data
 */
let map;
let placeGuideManager;

function initPage() {
  authenticateUser().then((userAuthenticationStatus) => {
    if (!userAuthenticationStatus.isLoggedIn) {
      location.replace(userAuthenticationStatus.loginUrl);
    } else {
      saveUserInDatabase().then((response) => {
        const menu = new Menu(undefined);
        fitContent();
        window.addEventListener('resize', function() {
          fitContent();
        });
        const mapWidget = new MapWidget();
        mapWidget.addGeolocationFunctionality();
        mapWidget.addSearchingFunctionality();
        mapWidget.centerAtCurrentLocation();
        map = mapWidget.map;
        const user = User.getUserFromQueryString();
        fillPortfolioDiv(user);
        placeGuideManager = new PlaceGuideManager(
            PlaceGuideManager.PAGE.USERS_PORTFOLIO, map, user.id);
      });
    }
  });
}

function fillPortfolioDiv(user) {
  if (user.imgKey != undefined) {
    setBlobKeyBackgroundToElement(user.imgKey, "portfolioImg");
  } else {
    const icon = document.createElement("icon");
    icon.classList
        .add("mdc-tab__icon", "material-icons", "no-portfolio-img-icon");
    icon.setAttribute("aria-hidden", true);
    icon.innerText = "no_photography";
    document.getElementById("portfolioImg").appendChild(icon);
  }
  if (user.name != undefined) {
    document.getElementById("portfolioName").innerText = user.name;
  } else {
    document.getElementById("portfolioName").innerText =
        user.email.substring(0, user.email.indexOf("@"));
  }
  if (user.selfIntroduction != undefined) {
    document.getElementById("portfolioSelfIntroduction").innerText =
        user.selfIntroduction;
  }
}

function fitContent() {
  setMapWidth();
  setContentHeight();
  setListHeight();
}
