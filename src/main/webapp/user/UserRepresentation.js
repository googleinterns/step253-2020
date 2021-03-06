/**
 * This class is responsible for displaying an icon for the creator of
 * placeguides, used in each placeguide info box. The icon creates a link to
 * the portfolio of the given user.
 */
class UserRepresentation {
  constructor(user) {
    this._userRepresentationDiv = UserRepresentation.createUserDiv(user);
  }

  get div() {
    return this._userRepresentationDiv;
  }

  static createUserDiv(user) {
    const userDiv = document.createElement("div");
    if (user.imgKey !== undefined) {
      if (user.name !== undefined) {
        userDiv.appendChild(
            UserRepresentation.createUserImg(user.imgKey, user.name));
      } else {
        userDiv.appendChild(
            UserRepresentation.createUserImg(user.imgKey,
                UserRepresentation.emailNamePart(user.email)));
      }
    } else {
      if (user.name !== undefined) {
        userDiv.appendChild(
            UserRepresentation.createUserIcon(user.name));
      } else {
        userDiv.appendChild(
            UserRepresentation.createUserIcon(
                UserRepresentation.emailNamePart(user.email)));
      }
    }
    userDiv.addEventListener("click", function() {
      if (user.publicPortfolio) {
        const queryString = user.queryString;
        const url = './usersPortfolio.html?' + queryString;
        window.location = url;
      } else {
        alert("The portfolio of this user is not public");
      }
    });
    return userDiv;
  }

  static createUserImg(imgKey, name) {
    const userImg = document.createElement("img");
    userImg.setAttribute("class", "user-img");
    const src = new URL("/serve-blob", document.URL);
    src.searchParams.append('blob-key', imgKey);
    userImg.setAttribute("src", src);
    userImg.setAttribute("title", "Owner: " + name);
    return userImg;
  }

  static createUserIcon(name) {
    const userIcon = document.createElement("i");
    userIcon.classList.add("material-icons",
        "mdc-icon-button",
        "mdc-card__action",
        "mdc-card__action--icon");
    userIcon.innerText = "account_circle";
    userIcon.setAttribute("title", "Owner: " + name);
    return userIcon;
  }

  static emailNamePart(email) {
    return email.substring(0, email.indexOf("@"));
  }
}
