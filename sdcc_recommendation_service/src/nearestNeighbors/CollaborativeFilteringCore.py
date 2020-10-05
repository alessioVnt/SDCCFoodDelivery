# collaborative filtering neural network
import numpy as np
import pandas as pd
import pickle

# WARNING: both insert and updating this module is a really discouraged action due to the high performance achieved
# and the extremely complexity reached. This module provide an initial elaboration of the raw data and a training of
# the model as written on top of the functions below. Run this only to create the model in the first place,
# then it'll never be used again. Elaborate_features() can be called as needed by the controller class to start itself.


# Elaborate dataraw and build features to feed k-NN
def elaborate_features():
    print('Start loading review.csv')
    data_dir = f'./dataRaw/'
    review_df = pd.read_csv(data_dir + 'yelp_review.csv', encoding="latin-1")
    review_df = review_df[['user_id', 'business_id', 'stars']]
    review_df = review_df.rename(columns={'stars': 'usr_rating'})
    review_df.dropna(inplace=True)

    print('Start loading business.csv')
    business_df = pd.read_csv(data_dir + 'yelp_business.csv', encoding="latin-1")
    business_df = business_df[['business_id', 'name', 'city', 'stars', 'review_count', 'categories']]
    business_df = business_df.rename(columns={'stars': 'restaurant_rating'})

    # include category which shows restaurants
    business_df = business_df[
        business_df['categories'].str.contains("Food|Coffee|Tea|Restaurants|Bakeries|Bars|Sports Bar"
                                               "|Pubs|Nighlife")]
    business_df.dropna(inplace=True)

    joined_restaurant_rating = pd.merge(business_df, review_df, on='business_id')
    restaurant_ratingCount = (joined_restaurant_rating.
        groupby(by=['name'])['restaurant_rating'].
        count().
        reset_index().
        rename(columns={'restaurant_rating': 'totalRatingCount'})
    [['name', 'totalRatingCount']]
        )

    rating_with_totalRatingCount = joined_restaurant_rating.merge(restaurant_ratingCount, left_on='name',
                                                                  right_on='name',
                                                                  how='left')

    pd.set_option('display.float_format', lambda x: '%.3f' % x)
    print(rating_with_totalRatingCount['totalRatingCount'].describe())

    # Calculate the minimum number of votes required to be in the dataset, m
    populatity_threshold = rating_with_totalRatingCount['totalRatingCount'].quantile(0.90)

    rating_popular_rest = rating_with_totalRatingCount.query('totalRatingCount >= @populatity_threshold')

    # Select top
    us_city_user_rating = rating_popular_rest[rating_popular_rest['city'].str.contains(
        "Las Vegas|Pheonix|Toronto|Scattsdale|Charlotte|Tempe|Chandler|Cleveland|Madison|Gilbert")]
    # us_canada_user_rating.head()
    us_city_user_rating = us_city_user_rating.drop_duplicates(['user_id', 'name'])
    restaurant_features = us_city_user_rating.pivot(index='name', columns='user_id', values='restaurant_rating').fillna(
        0)
    return restaurant_features


# Initial steps to build the k-nn system and initial training of the model
# Run only for develop start
def initial_data_loader():

    restaurant_features = elaborate_features()

    from scipy.sparse import csr_matrix

    restaurant_features_matrix = csr_matrix(restaurant_features.values)

    from sklearn.neighbors import NearestNeighbors

    knn_recomm = NearestNeighbors(metric='cosine', algorithm='brute')
    knn_recomm.fit(restaurant_features_matrix)

    # save the model to pickle
    filename = '..\..\model\.finalized_model.sav'
    pickle.dump(knn_recomm, open(filename, 'wb'))

    # Only for test purpose
    randomChoice = np.random.choice(restaurant_features.shape[0])

    distances, indices = knn_recomm.kneighbors(restaurant_features.iloc[randomChoice].values.reshape(1, -1),
                                               n_neighbors=11)

    for i in range(0, len(distances.flatten())):
        if i == 0:
            print('Recommendations for Restaurant {0}:\n'.format(
                restaurant_features.index[randomChoice]))
        else:
            print('{0}: {1}'.format(i, restaurant_features.index[indices.flatten()[i]]))


if __name__ == '__main__':
    from src.service.RecommendationService import RecommendationService
    reco = RecommendationService()
    print(reco.get_recommendation_by_user_id(0))
