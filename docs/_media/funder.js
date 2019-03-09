(function() {
  function embed () {
    var evt = new Event('codefund');

    function trackUplift() {

    };

    function detectUplift(count) {

    }

    try {
      var targetElement = document.getElementById('codefund');
      if (targetElement == null) { targetElement = document.getElementById('codefund_ad'); }
      targetElement.innerHTML = '<div id="cf" style="max-width: 330px; margin: 0 auto;"> <div class="cf-wrapper" style="display: block; overflow: hidden; font-size: 14px; line-height: 1.4; font-family: Helvetica; padding: 15px;" align="left"> <div class="clearfix" style="overflow: auto;"> <a data-href="campaign_url" class="cf-img-wrapper" target="_blank" rel="noopener" style="float: left; margin-right: 15px;"> <img class="cf-img" src="images/logo2.png" style="vertical-align: middle; max-width: 130px; border: none;"> </a> <a data-href="campaign_url" class="cf-text" target="_blank" rel="noopener" style="color: #333; text-decoration: none;"> <strong>Elasticsearch Bboss</strong> <span>makes elasticsearch program easy to build, with minimal configure</span> </a> </div> <a href="https://www.bbossgroups.com" data-target="powered_by_url" class="cf-powered-by" target="_blank" rel="noopener" style="margin-top: 8px; background-color: hsla(0, 0%, 0%, 0.05); text-align: center; display: block; font-size: 9px; font-weight: 400; letter-spacing: 0.5px; line-height: 2.2; text-transform: uppercase; color: hsla(0, 0%, 0%, 0.8); text-decoration: none; padding: 10px auto;"> <em>BBoss</em> ad by BBossgroup   </a> </div> </div> <style>#cf .clearfix::after { content: ""; clear: both; display: table; } </style>';
      //targetElement.querySelector('img[data-src="impression_url"]').src = 'https://codefund.app/display/1928f223-8cd8-49ad-a725-4afd8513b9a7.gif?template=docsify&theme=light';
      //targetElement.querySelector('a[data-target="powered_by_url"]').href = 'https://codefund.app/invite/aaq8ITeum7E';
      //targetElement.querySelectorAll('a[data-href="campaign_url"]').forEach(function (a) { a.href = 'https://codefund.app/impressions/1928f223-8cd8-49ad-a725-4afd8513b9a7/click?campaign_id=159'; });
      evt.detail = { status: 'ok', house: false };
      detectUplift(1);
    } catch (e) {
      console.log('CodeFund detected an error! Please verify an element exists with id="codefund". ' + e.message);
      evt.detail = { status: 'error', message: e.message };
    }
    document.removeEventListener('DOMContentLoaded', embed);
    window.dispatchEvent(evt);
  };
  (document.readyState === 'loading') ? document.addEventListener('DOMContentLoaded', embed) : embed();
})();
